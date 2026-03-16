#!/bin/bash
# =============================================================================
# Apply Index Lifecycle Management (ILM) Policy to Elasticsearch
# =============================================================================
# Usage: ./apply-ilm-policy.sh [ELASTICSEARCH_URL]
# Default ELASTICSEARCH_URL: http://localhost:9200
# =============================================================================

ES_URL="${1:-http://localhost:9200}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Waiting for Elasticsearch to be ready..."
until curl -s "${ES_URL}/_cluster/health" | grep -q '"status":"green"\|"status":"yellow"'; do
    echo "  Elasticsearch not ready yet, retrying in 5s..."
    sleep 5
done

echo "Elasticsearch is ready. Applying ILM policy..."

# Create the ILM policy
curl -s -X PUT "${ES_URL}/_ilm/policy/ftgo-logs-policy" \
    -H "Content-Type: application/json" \
    -d @"${SCRIPT_DIR}/index-lifecycle-policy.json" \
    | python3 -m json.tool 2>/dev/null

echo ""

# Create the index template with ILM policy attached
curl -s -X PUT "${ES_URL}/_index_template/ftgo-logs-template" \
    -H "Content-Type: application/json" \
    -d '{
  "index_patterns": ["ftgo-logs-*", "ftgo-k8s-logs-*"],
  "template": {
    "settings": {
      "index.lifecycle.name": "ftgo-logs-policy",
      "index.lifecycle.rollover_alias": "ftgo-logs",
      "number_of_shards": 1,
      "number_of_replicas": 0
    },
    "mappings": {
      "properties": {
        "@timestamp": { "type": "date" },
        "message": { "type": "text" },
        "level": { "type": "keyword" },
        "logger_name": { "type": "keyword" },
        "thread_name": { "type": "keyword" },
        "service": { "type": "keyword" },
        "correlationId": { "type": "keyword" },
        "serviceName": { "type": "keyword" },
        "requestMethod": { "type": "keyword" },
        "requestUri": { "type": "keyword" },
        "clientIp": { "type": "ip" },
        "userId": { "type": "keyword" },
        "traceId": { "type": "keyword" },
        "spanId": { "type": "keyword" },
        "stack_trace": { "type": "text" }
      }
    }
  }
}' \
    | python3 -m json.tool 2>/dev/null

echo ""
echo "ILM policy and index template applied successfully!"
echo ""
echo "Retention policy summary:"
echo "  Hot phase:    0-7 days   (rollover at 5GB or 1 day)"
echo "  Warm phase:   7-30 days  (shrink + force merge)"
echo "  Cold phase:   30-90 days (frozen indices)"
echo "  Delete phase: 90+ days   (indices deleted)"
