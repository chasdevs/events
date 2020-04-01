#!/bin/bash

set -e

export SPRING_PROFILES_ACTIVE=prod

JAR=$(ls build/libs/* | grep -e events.*jar$ | head -1)
if [ -z "$JAR" ]; then
    echo "Compiling jar..."
    JAR=$(gradle -q build -x test)
fi

echo "Validating local schemas..."
java -jar ${JAR} validate

# DEMO: Not actually checking compatibility
echo "Checking compatibility against schema registry..."
echo java -jar ${JAR} test-compatibility