#!/usr/bin/env bash

START_ZOOKEEPER=false
START_KAFKA=false

if [[ ! -z "$1" ]]; then
    INSTALL_DIR=$1
else
    INSTALL_DIR=$(pwd)
fi


echo "installing kafka to $INSTALL_DIR"
if [[ -d ${INSTALL_DIR} ]]; then
    cd ${INSTALL_DIR}
else
    mkdir ${INSTALL_DIR} && cd ${INSTALL_DIR}
fi

echo "start zookeeper? (y/n)"
read start_choice

if [[ ${start_choice} == 'y' ]]; then
  START_ZOOKEEPER=true
  echo "zookeeper will be started after installation"
fi

echo "start kafka? (y/n)"
read start_choice

if [[ ${start_choice} == 'y' ]]; then
  START_KAFKA=true
  echo "kafka will be started after installation"
fi

echo "downloading kafka"
wget http://apache.mirrors.ionfish.org/kafka/2.2.0/kafka_2.12-2.2.0.tgz

echo "unpacking kafka"
tar -xzf kafka_2.12-2.2.0.tgz

cd kafka_2.12-2.2.0

if [[ ${START_ZOOKEEPER} == true ]]; then
  echo "starting zookeeper"
  touch zookeeper.log
  ./bin/zookeeper-server-start.sh config/zookeeper.properties 2>&1 > zookeeper.log & echo "zookeeper started with pid: $!" &
fi

if [[ ${START_KAFKA} == true ]]; then
  echo "starting kafka"
  touch kafka.log
  ./bin/kafka-server-start.sh config/server.properties 2>&1 > kafka.log & echo "kafka started with pid: $!" &
fi

exit