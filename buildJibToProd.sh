#!/bin/bash

CUR_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

if [[ "$OSTYPE" == "linux-gnu" ]]; then
  jhome=$(dirname /usr/lib/jvm/*11*/bin)
elif [[ "$OSTYPE" == "darwin"* ]]; then
  jhome=$(/usr/libexec/java_home -v 11)
else
  echo "no java 11 found"
  exit 1
fi

JAVA_HOME=$jhome "$CUR_DIR"/gradlew clean jib \
  -Djib-json-key-file="$CUR_DIR/kaif-deploy/secret/kaif-id-93d44b502305.json"