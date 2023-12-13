#!/bin/sh

docker-compose up --build --no-deps --wait -d app-1
docker-compose up --build --no-deps --wait -d app-2