{{/*
=============================================================================
FTGO Helm Chart — Template Helpers
=============================================================================
*/}}

{{/*
Expand the name of the chart.
*/}}
{{- define "ftgo.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
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

{{/*
Chart label.
*/}}
{{- define "ftgo.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels for a service.
Usage: {{ include "ftgo.labels" (dict "name" "ftgo-order-service" "context" $) }}
*/}}
{{- define "ftgo.labels" -}}
helm.sh/chart: {{ include "ftgo.chart" .context }}
app.kubernetes.io/managed-by: {{ .context.Release.Service }}
app.kubernetes.io/part-of: ftgo-platform
app.kubernetes.io/version: {{ .context.Chart.AppVersion | quote }}
{{ include "ftgo.selectorLabels" . }}
{{- end }}

{{/*
Selector labels for a service.
Usage: {{ include "ftgo.selectorLabels" (dict "name" "ftgo-order-service" "context" $) }}
*/}}
{{- define "ftgo.selectorLabels" -}}
app.kubernetes.io/name: {{ .name }}
app.kubernetes.io/instance: {{ .context.Release.Name }}
{{- end }}

{{/*
Resolve the image for a service.
Usage: {{ include "ftgo.serviceImage" (dict "global" .Values.global "service" .Values.orderService "serviceName" "ftgo-order-service") }}
*/}}
{{- define "ftgo.serviceImage" -}}
{{- $registry := .global.imageRegistry -}}
{{- $repo := default (printf "%s/%s" $registry .serviceName) .service.image.repository -}}
{{- $tag := default .global.imageTag .service.image.tag -}}
{{- printf "%s:%s" $repo $tag -}}
{{- end }}

{{/*
Resolve replicas for a service (service override > serviceDefaults).
Usage: {{ include "ftgo.replicas" (dict "service" .Values.orderService "defaults" .Values.serviceDefaults) }}
*/}}
{{- define "ftgo.replicas" -}}
{{- if .service.replicas }}
{{- .service.replicas }}
{{- else }}
{{- .defaults.replicas }}
{{- end }}
{{- end }}

{{/*
Merge environment variables from serviceDefaults and per-service overrides.
Usage: {{ include "ftgo.envVars" (dict "defaults" .Values.serviceDefaults "service" .Values.orderService "serviceName" "ftgo-order-service" "port" 8081) }}
*/}}
{{- define "ftgo.envVars" -}}
{{- $merged := merge (default dict .service.env) .defaults.env -}}
- name: SERVER_PORT
  value: {{ .port | quote }}
- name: SPRING_APPLICATION_NAME
  value: {{ .serviceName | quote }}
{{- range $key, $val := $merged }}
- name: {{ $key }}
  value: {{ $val | quote }}
{{- end }}
- name: SPRING_DATASOURCE_URL
  valueFrom:
    configMapKeyRef:
      name: ftgo-common-config
      key: database-url
- name: SPRING_DATASOURCE_USERNAME
  valueFrom:
    secretKeyRef:
      name: ftgo-mysql-credentials
      key: username
- name: SPRING_DATASOURCE_PASSWORD
  valueFrom:
    secretKeyRef:
      name: ftgo-mysql-credentials
      key: password
- name: FTGO_SECURITY_JWT_SECRET
  valueFrom:
    secretKeyRef:
      name: ftgo-jwt-secret
      key: jwt-secret
{{- end }}
