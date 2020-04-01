#!/bin/bash

ENV="${ENV:-default}"

JAR=`ls -d build/libs/* 2> /dev/null | grep ".jar"`
if [[ -z "$JAR" ]]; then
    echo "No JAR exists. Please build the JAR first by running 'gradle clean build'"
    exit 1
else
    SPRING_PROFILES_ACTIVE=${ENV} java -jar ${JAR} $@
fi
