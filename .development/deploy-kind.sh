#!/bin/bash

./gradlew quarkusBuild

docker build -f src/main/docker/Dockerfile.jvm -t explorviz/adapter-service-jvm .

kind load docker-image --name explorviz-dev explorviz/adapter-service-jvm:latest

kubectl delete --context kind-explorviz-dev -f manifest.yml
kubectl apply --context kind-explorviz-dev -f manifest.yml
