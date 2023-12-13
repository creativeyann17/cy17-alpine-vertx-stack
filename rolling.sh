#!/bin/sh

docker-compose stop app-1
docker-compose up --build -d app-1
sleep 30
docker-compose stop app-2
docker-compose up --build -d app-2