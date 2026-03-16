#!/bin/bash
# =============================================================================
# Import Kibana Dashboards for FTGO Logging
# =============================================================================
# Usage: ./import-dashboards.sh [KIBANA_URL]
# Default KIBANA_URL: http://localhost:5601
# =============================================================================

KIBANA_URL="${1:-http://localhost:5601}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DASHBOARD_FILE="${SCRIPT_DIR}/dashboards.ndjson"

echo "Waiting for Kibana to be ready..."
until curl -s "${KIBANA_URL}/api/status" | grep -q '"state":"green"\|"overall"'; do
    echo "  Kibana not ready yet, retrying in 5s..."
    sleep 5
done

echo "Kibana is ready. Importing dashboards..."

curl -s -X POST "${KIBANA_URL}/api/saved_objects/_import?overwrite=true" \
    -H "kbn-xsrf: true" \
    --form file=@"${DASHBOARD_FILE}" \
    | python3 -m json.tool 2>/dev/null || echo "(response printed above)"

echo ""
echo "Dashboard import complete!"
echo "Access Kibana at: ${KIBANA_URL}"
echo "Dashboard: ${KIBANA_URL}/app/dashboards#/view/ftgo-service-logs-dashboard"
