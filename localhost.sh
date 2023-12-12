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
        nano .env
    fi
}

create_lograte_nginx()
{
    if [ ! -f /etc/logrotate.d/nginx ]
    then
        sudo sh -c "echo '/var/log/nginx/*.log {\n\trotate 7\n\tmissingok\n\tcopytruncate\n\trotate 52\n\tmaxsize 100M\n\tcompress\n\tdelaycompress\n}' > /etc/logrotate.d/nginx"
        sudo logrotate --force /etc/logrotate.d/nginx
    fi
}

generate_dev_certs
prepare_env_file
create_lograte_nginx

docker-compose down && docker-compose up --build