#!/bin/sh

docker-compose stop app-1
docker-compose up --build -d
sleep 30
docker-compose stop app-2
docker-compose up --build -d