{{- define "ftgo.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "ftgo.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{- define "ftgo.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "ftgo.labels" -}}
helm.sh/chart: {{ include "ftgo.chart" . }}
app.kubernetes.io/name: {{ include "ftgo.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/part-of: ftgo
{{- end }}

{{- define "ftgo.selectorLabels" -}}
app.kubernetes.io/name: {{ include "ftgo.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Prefix for component names, at most 46 chars so "<prefix>-mysql" stays within the
52-char StatefulSet name limit. Longer fullnames keep a hash suffix to stay unique.
*/}}
{{- define "ftgo.componentPrefix" -}}
{{- $full := include "ftgo.fullname" . }}
{{- if le (len $full) 46 }}
{{- $full }}
{{- else }}
{{- printf "%s-%s" ($full | trunc 39 | trimSuffix "-") ($full | sha256sum | trunc 6) }}
{{- end }}
{{- end }}

{{- define "ftgo.app.fullname" -}}
{{- printf "%s-application" (include "ftgo.componentPrefix" .) }}
{{- end }}

{{- define "ftgo.app.labels" -}}
{{ include "ftgo.labels" . }}
app.kubernetes.io/component: application
{{- end }}

{{- define "ftgo.app.selectorLabels" -}}
{{ include "ftgo.selectorLabels" . }}
app.kubernetes.io/component: application
{{- end }}

{{- define "ftgo.app.image" -}}
{{- printf "%s:%s" .Values.app.image.repository (.Values.app.image.tag | default .Chart.AppVersion) }}
{{- end }}

{{- define "ftgo.mysql.fullname" -}}
{{- printf "%s-mysql" (include "ftgo.componentPrefix" .) }}
{{- end }}

{{- define "ftgo.mysql.labels" -}}
{{ include "ftgo.labels" . }}
app.kubernetes.io/component: mysql
{{- end }}

{{- define "ftgo.mysql.selectorLabels" -}}
{{ include "ftgo.selectorLabels" . }}
app.kubernetes.io/component: mysql
{{- end }}

{{- define "ftgo.mysql.image" -}}
{{- printf "%s:%s" .Values.mysql.image.repository .Values.mysql.image.tag }}
{{- end }}

{{/* Secret holding the database credentials used by the app (keys: mysql-user, mysql-password). */}}
{{- define "ftgo.db.secretName" -}}
{{- if .Values.mysql.enabled }}
{{- .Values.mysql.auth.existingSecret | default (include "ftgo.mysql.fullname" .) }}
{{- else }}
{{- .Values.externalDatabase.existingSecret | default (printf "%s-external-db" (include "ftgo.componentPrefix" .)) }}
{{- end }}
{{- end }}

{{/*
Base64 password for the in-chart MySQL Secret: the value already stored in the Secret, else the supplied value,
else random. MySQL keeps the password it was initialised with, so a supplied value that differs from the stored one fails.
Args: (list $existingData key suppliedValue valuePath)
*/}}
{{- define "ftgo.mysql.retainedPassword" -}}
{{- $stored := get (index . 0) (index . 1) }}
{{- $supplied := index . 2 | b64enc }}
{{- if and $stored $supplied (ne $stored $supplied) }}
{{- fail (printf "%s differs from the password already stored in the MySQL Secret; MySQL only applies it on an empty volume. Rotate it in MySQL and the Secret first, or leave %s empty." (index . 3) (index . 3)) }}
{{- end }}
{{- $stored | default $supplied | default (randAlphaNum 24 | b64enc) }}
{{- end }}

{{- define "ftgo.db.createSecret" -}}
{{- if .Values.mysql.enabled }}
{{- if not .Values.mysql.auth.existingSecret }}true{{ end }}
{{- else }}
{{- if not .Values.externalDatabase.existingSecret }}true{{ end }}
{{- end }}
{{- end }}

{{- define "ftgo.db.host" -}}
{{- if .Values.mysql.enabled }}
{{- include "ftgo.mysql.fullname" . }}
{{- else }}
{{- required "externalDatabase.host is required when mysql.enabled=false" .Values.externalDatabase.host }}
{{- end }}
{{- end }}

{{- define "ftgo.db.port" -}}
{{- if .Values.mysql.enabled }}{{ .Values.mysql.service.port }}{{ else }}{{ .Values.externalDatabase.port }}{{ end }}
{{- end }}

{{- define "ftgo.db.name" -}}
{{- if .Values.mysql.enabled }}{{ .Values.mysql.auth.database }}{{ else }}{{ .Values.externalDatabase.database }}{{ end }}
{{- end }}

{{/*
JDBC query string: app.jdbcParams if set, otherwise plaintext for the in-chart MySQL (no CA-signed cert)
and TLS required for an external database.
*/}}
{{- define "ftgo.jdbcParams" -}}
{{- .Values.app.jdbcParams | default (ternary "useSSL=false&allowPublicKeyRetrieval=true" "sslMode=REQUIRED" .Values.mysql.enabled) }}
{{- end }}

{{- define "ftgo.datasourceUrl" -}}
{{- printf "jdbc:mysql://%s:%s/%s?%s" (include "ftgo.db.host" .) (include "ftgo.db.port" .) (include "ftgo.db.name" .) (include "ftgo.jdbcParams" .) }}
{{- end }}

{{- define "ftgo.migrations.image" -}}
{{- printf "%s:%s" .Values.migrations.image.repository (.Values.migrations.image.tag | default .Chart.AppVersion) }}
{{- end }}

{{/*
Flyway container, shared by the app initContainer and the hook Job.
Connects as the application DB user, which owns ftgo.* (GRANT ALL in schema.sql).
*/}}
{{- define "ftgo.migrations.container" -}}
- name: flyway-migrate
  image: {{ include "ftgo.migrations.image" . | quote }}
  imagePullPolicy: {{ .Values.migrations.image.pullPolicy }}
  args: ["migrate"]
  securityContext:
    {{- toYaml .Values.migrations.securityContext | nindent 4 }}
  env:
    - name: FLYWAY_URL
      value: {{ include "ftgo.datasourceUrl" . | quote }}
    - name: FLYWAY_USER
      valueFrom:
        secretKeyRef:
          name: {{ include "ftgo.db.secretName" . }}
          key: mysql-user
    - name: FLYWAY_PASSWORD
      valueFrom:
        secretKeyRef:
          name: {{ include "ftgo.db.secretName" . }}
          key: mysql-password
    - name: FLYWAY_CONNECT_RETRIES
      value: {{ .Values.migrations.connectRetries | quote }}
    - name: FLYWAY_CONNECT_RETRIES_INTERVAL
      value: {{ .Values.migrations.connectRetriesInterval | quote }}
    - name: JAVA_ARGS
      value: {{ .Values.migrations.javaArgs | quote }}
  resources:
    {{- toYaml .Values.migrations.resources | nindent 4 }}
{{- end }}

{{- define "ftgo.tests.image" -}}
{{- $img := printf "%s:%s" .Values.tests.image.repository .Values.tests.image.tag }}
{{- if .Values.tests.image.digest }}{{ printf "%s@%s" $img .Values.tests.image.digest }}{{ else }}{{ $img }}{{ end }}
{{- end }}
