
# Host DNS name doesn't resolve in Docker alpine images

export DOCKER_HOST_IP=$(hostname -I | sed -e 's/ .*//g')
export TERM=dumb



# CI-only credentials for the ftgo-application security layer (must match the end-to-end test defaults)
export FTGO_API_USERNAME=test-api
export FTGO_API_PASSWORD=test-api-pw
export FTGO_OPERATOR_USERNAME=test-operator
export FTGO_OPERATOR_PASSWORD=test-operator-pw
