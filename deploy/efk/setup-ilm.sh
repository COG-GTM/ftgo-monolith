#!/bin/bash
# =============================================================================
# EFK Stack ILM Setup Script — FTGO Platform
# =============================================================================
# Sets up Elasticsearch Index Lifecycle Management policies and index templates
# for FTGO log retention.
#
# Usage:
#   ./setup-ilm.sh [environment]
#
# Arguments:
#   environment — dev (default), staging, or prod
#
# Prerequisites:
#   - Elasticsearch must be running and accessible
#   - curl must be installed
# =============================================================================

set -euo pipefail

ENVIRONMENT="${1:-dev}"
ES_HOST="${ELASTICSEARCH_HOST:-localhost}"
ES_PORT="${ELASTICSEARCH_PORT:-9200}"
ES_URL="http://${ES_HOST}:${ES_PORT}"

echo "=== FTGO EFK Stack Setup ==="
echo "Environment: ${ENVIRONMENT}"
echo "Elasticsearch: ${ES_URL}"
echo ""

# Wait for Elasticsearch to be ready
echo "Waiting for Elasticsearch..."
until curl -sf "${ES_URL}/_cluster/health" > /dev/null 2>&1; do
    echo "  Elasticsearch not ready, retrying in 5s..."
    sleep 5
done
echo "  Elasticsearch is ready."
echo ""

# --- Create ILM Policy ---
echo "Creating ILM policy for ${ENVIRONMENT}..."

case "${ENVIRONMENT}" in
    dev)
        DELETE_AGE="7d"
        ROLLOVER_SIZE="5gb"
        ;;
    staging)
        DELETE_AGE="30d"
        ROLLOVER_SIZE="10gb"
        ;;
    prod)
        DELETE_AGE="90d"
        ROLLOVER_SIZE="20gb"
        ;;
    *)
        echo "Unknown environment: ${ENVIRONMENT}. Using dev defaults."
        DELETE_AGE="7d"
        ROLLOVER_SIZE="5gb"
        ;;
esac

curl -sf -X PUT "${ES_URL}/_ilm/policy/ftgo-logs-${ENVIRONMENT}" \
    -H 'Content-Type: application/json' \
    -d "{
  \"policy\": {
    \"phases\": {
      \"hot\": {
        \"min_age\": \"0ms\",
        \"actions\": {
          \"rollover\": {
            \"max_age\": \"1d\",
            \"max_size\": \"${ROLLOVER_SIZE}\"
          }
        }
      },
      \"delete\": {
        \"min_age\": \"${DELETE_AGE}\",
        \"actions\": {
          \"delete\": {}
        }
      }
    }
  }
}" && echo " Done." || echo " Failed!"

echo ""

# --- Create Index Template ---
echo "Creating index template..."

curl -sf -X PUT "${ES_URL}/_index_template/ftgo-logs" \
    -H 'Content-Type: application/json' \
    -d "{
  \"index_patterns\": [\"ftgo-logs-*\"],
  \"template\": {
    \"settings\": {
      \"number_of_shards\": 1,
      \"number_of_replicas\": 0,
      \"index.lifecycle.name\": \"ftgo-logs-${ENVIRONMENT}\",
      \"index.lifecycle.rollover_alias\": \"ftgo-logs\"
    },
    \"mappings\": {
      \"properties\": {
        \"@timestamp\": { \"type\": \"date\" },
        \"level\": { \"type\": \"keyword\" },
        \"service\": { \"type\": \"keyword\" },
        \"logger\": { \"type\": \"keyword\" },
        \"thread\": { \"type\": \"keyword\" },
        \"message\": { \"type\": \"text\", \"fields\": { \"keyword\": { \"type\": \"keyword\", \"ignore_above\": 1024 } } },
        \"stackTrace\": { \"type\": \"text\" },
        \"traceId\": { \"type\": \"keyword\" },
        \"spanId\": { \"type\": \"keyword\" },
        \"correlationId\": { \"type\": \"keyword\" },
        \"userId\": { \"type\": \"keyword\" },
        \"requestId\": { \"type\": \"keyword\" },
        \"requestMethod\": { \"type\": \"keyword\" },
        \"requestUri\": { \"type\": \"keyword\" }
      }
    }
  },
  \"priority\": 200
}" && echo " Done." || echo " Failed!"

echo ""

# --- Create Initial Index with Alias ---
echo "Creating initial index with alias..."

curl -sf -X PUT "${ES_URL}/ftgo-logs-000001" \
    -H 'Content-Type: application/json' \
    -d "{
  \"aliases\": {
    \"ftgo-logs\": {
      \"is_write_index\": true
    }
  }
}" && echo " Done." || echo " Failed!"

echo ""

# --- Create Kibana Index Pattern ---
echo "Creating Kibana index pattern..."

KIBANA_URL="${KIBANA_URL:-http://${ES_HOST}:5601}"
until curl -sf "${KIBANA_URL}/api/status" > /dev/null 2>&1; do
    echo "  Kibana not ready, retrying in 5s..."
    sleep 5
done

curl -sf -X POST "${KIBANA_URL}/api/saved_objects/index-pattern/ftgo-logs-*" \
    -H 'kbn-xsrf: true' \
    -H 'Content-Type: application/json' \
    -d "{
  \"attributes\": {
    \"title\": \"ftgo-logs-*\",
    \"timeFieldName\": \"@timestamp\"
  }
}" && echo " Done." || echo " Failed!"

echo ""
echo "=== FTGO EFK Stack Setup Complete ==="
echo ""
echo "Access Kibana at: ${KIBANA_URL}"
echo "Index pattern: ftgo-logs-*"
echo "ILM policy: ftgo-logs-${ENVIRONMENT} (retention: ${DELETE_AGE})"
