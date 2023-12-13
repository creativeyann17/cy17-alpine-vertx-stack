#!/bin/bash

STACK_NAME="stack"
ENV="${1}"

showUsage()
{
  echo "Stack managment"
  echo
  echo "Usage:"
  echo "  sh ./stack.sh [COMMAND]"
  echo "  ./stack.sh [COMMAND] (requires chmod +x ./stack.sh)"
  echo 
  echo "Commands:"
  echo "  init          prepare the stack to be deployed"
  echo "  dev           start the stack in DEV mode"
  echo "  prod          start the stack in PROD mode"
  echo "  rolling       perform a stack rolling"
  echo "  logs          follow stack logs"
  echo "  status        show current stack status"
  echo "  stop          stop the stack"
  echo
}

prepare_env_file()
{
    echo "==> check .env file"
    if [ ! -f .env ]
    then
        cp .env.dev .env
        nano .env
    fi
}

create_lograte_nginx()
{
    echo "==> ngxing logs config"
    if [ ! -f /etc/logrotate.d/nginx ]
    then
        sudo sh -c "echo '/var/log/nginx/*.log {\n\tdaily\n\trotate 7\n\tmissingok\n\tnotifempty\n\tdelaycompress\n\tcompress\n\tsize 100M\n}' > /etc/logrotate.d/nginx"
        sudo logrotate --force /etc/logrotate.d/nginx
    fi
}

generate_dev_certs() {
  echo "==> generate dev certificates"
  DOMAIN=localhost
  CERTS_PATH=./certs/live/$DOMAIN

  mkdir -p $CERTS_PATH
  if [ ! -f $CERTS_PATH/fullchain.pem ]
  then
      openssl req -x509 -out $CERTS_PATH/fullchain.pem -keyout $CERTS_PATH/privkey.pem -newkey rsa:2048 -nodes -sha256 -subj /CN=$DOMAIN -subj "/C=US/ST=OH/L=Cincinnati/O=Your Company, Inc./OU=IT/CN=$DOMAIN"
      echo "==> self-signed '$DOMAIN' certificates generated"
  fi
}

init()
{
  prepare_env_file
  create_lograte_nginx
}

start()
{
  docker-compose -p $STACK_NAME up --build $1
}

stop()
{
  docker-compose -p $STACK_NAME down --rmi all
}

dev()
{
  init
  generate_dev_certs
  stop
  start
}

prod()
{
  # TODO generate certs automatically with certbot
  init
  start "-d --wait"
}

rolling()
{
  start "--no-deps --wait -d app-1"
  start "--no-deps --wait -d app-2"
}

logs()
{
  tail -f /var/log/app/app.log
}

status()
{
  docker ps --format "table {{.ID}}\t{{.Names}}\t{{.Status}}" | grep $STACK_NAME
}

case "$1" in
  "init")
  init
  ;;
  "dev")
  dev
  ;;
  "prod")
  prod
  ;;
  "rolling")
  rolling
  ;;
  "logs")
  logs
  ;;
  "status")
  status
  ;;
  "stop")
  stop
  ;;
  *) 
  showUsage 
  ;;
esac