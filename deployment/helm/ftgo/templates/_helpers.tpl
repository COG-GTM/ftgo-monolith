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

{{- define "ftgo.mysql.secretName" -}}
{{- .Values.mysql.auth.existingSecret | default (include "ftgo.mysql.fullname" .) }}
{{- end }}

{{- define "ftgo.datasourceUrl" -}}
{{- printf "jdbc:mysql://%s:%v/%s?%s" (include "ftgo.mysql.fullname" .) .Values.mysql.service.port .Values.mysql.auth.database .Values.app.jdbcParams }}
{{- end }}
