#! /bin/bash -e

if [ -z "$FTGO_SECURITY_JWT_SECRET" ]; then
  echo "FTGO_SECURITY_JWT_SECRET must be set (>= 32 bytes) and match the running application" >&2
  exit 1
fi

./gradlew :ftgo-end-to-end-tests:cleanTest :ftgo-end-to-end-tests:test
