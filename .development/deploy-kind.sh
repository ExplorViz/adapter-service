#!/bin/bash

./gradlew quarkusBuild

docker build -f src/main/docker/Dockerfile.jvm -t explorviz/adapter-service-jvm .

kind load docker-image explorviz/adapter-service-jvm:latest

kubectl apply -f manifest.yml