# FTGO Centralized Logging Infrastructure

Centralized logging stack using the ELK (Elasticsearch, Logstash, Kibana) stack with Filebeat for log collection.

## Architecture

```
Services (JSON logs via Logback)
    │
    ├─► Logstash TCP/UDP (port 5000) ─► Elasticsearch ─► Kibana
    │
    └─► Filebeat (Docker log collection) ─► Logstash (port 5044) ─►─┘
```

## Quick Start

```bash
docker compose up -d
```

## Services

| Service        | Port  | Description                    |
|----------------|-------|--------------------------------|
| Elasticsearch  | 9200  | Search and analytics engine    |
| Logstash       | 5044  | Beats input (Filebeat)         |
| Logstash       | 5000  | TCP/UDP input (direct logging) |
| Logstash       | 9600  | Monitoring API                 |
| Kibana         | 5601  | Visualization and dashboards   |

## Log Pipeline

1. **Filebeat** collects Docker container logs from FTGO services
2. **Logstash** receives logs via Beats protocol (port 5044) or TCP/UDP (port 5000)
3. **Logstash** parses structured JSON logs, extracts trace/span IDs, and enriches metadata
4. **Elasticsearch** stores and indexes logs with the `ftgo-logs-*` index pattern
5. **Kibana** provides dashboards for log visualization and trace correlation

## Kibana Dashboards

Import dashboards from `kibana/dashboards/`:

```bash
curl -X POST "http://localhost:5601/api/saved_objects/_import" \
  -H "kbn-xsrf: true" \
  --form file=@kibana/dashboards/ftgo-service-logs-dashboard.ndjson
```

### Included Dashboards

- **FTGO Service Logs Overview** - Main dashboard with log volume, error trends, and service breakdown
- **Log Volume by Service** - Pie chart showing log distribution across services
- **Error Trends** - Line chart of error rates over time per service
- **Log Level Distribution** - Histogram of log levels
- **Trace Correlation Search** - Saved search for looking up logs by trace ID

## Index Lifecycle Management

The `elasticsearch/ilm-policy.json` configures log retention:

- **Hot**: Active writes, rollover after 1 day or 10GB
- **Warm**: After 2 days, shrink and force merge
- **Cold**: After 7 days, reduced priority
- **Delete**: After 30 days

Apply the ILM policy:

```bash
curl -X PUT "http://localhost:9200/_ilm/policy/ftgo-logs-policy" \
  -H "Content-Type: application/json" \
  -d @elasticsearch/ilm-policy.json
```

## Integration with Services

Services using `libs/ftgo-logging` automatically emit structured JSON logs compatible with this pipeline. Configure the Logstash appender in your service:

```properties
ftgo.logging.logstash.enabled=true
ftgo.logging.logstash.host=localhost
ftgo.logging.logstash.port=5000
```
