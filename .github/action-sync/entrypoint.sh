#!/bin/bash

set -e

export SPRING_PROFILES_ACTIVE=prod

JAR=$(ls build/libs/* | grep -e events.*jar$ | head -1)
if [ -z "$JAR" ]; then
    echo "Compiling jar..."
    JAR=$(gradle -q build -x test)
fi

echo "Synchronizing with schema registry..."

# DEMO; not actually syncing with a schema registry
echo java -jar ${JAR} sync --force