#!/usr/bin/env bash
set -e
TAG="garmento.io/try-on-server"

./gradlew clean bootJar
docker build -t $TAG .
