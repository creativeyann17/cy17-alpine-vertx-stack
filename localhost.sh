#!/bin/sh

DOMAIN=localhost
CERTS_PATH=./certs/live/$DOMAIN

generate_dev_certs()
{
    mkdir -p $CERTS_PATH
    if [ ! -f $CERTS_PATH/fullchain.pem ]
    then
        openssl req -x509 -out $CERTS_PATH/fullchain.pem -keyout $CERTS_PATH/privkey.pem -newkey rsa:2048 -nodes -sha256 -subj /CN=$DOMAIN -subj "/C=US/ST=OH/L=Cincinnati/O=Your Company, Inc./OU=IT/CN=$DOMAIN"
        echo "Fake '$DOMAIN' certificates generated"
    fi
}

prepare_env_file()
{
    if [ ! -f .env ]
    then
        cp .env.dev .env
    fi
}

generate_dev_certs
prepare_env_file

docker-compose down && docker-compose up --build