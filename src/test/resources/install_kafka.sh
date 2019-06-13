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


echo "zookeeper will be started after installation"
echo "kafka will be started after installation"


echo "downloading kafka"
wget https://archive.apache.org/dist/kafka/0.10.2.2/kafka_2.11-0.10.2.2.tgz

echo "unpacking kafka"
tar -xzf kafka_2.11-0.10.2.2.tgz

cd kafka_2.11-0.10.2.2


echo "starting zookeeper"
touch zookeeper.log
./bin/zookeeper-server-start.sh config/zookeeper.properties 2>&1 > zookeeper.log & echo "zookeeper started with pid: $!" &

sleep 2s

echo "starting kafka"
touch kafka.log
./bin/kafka-server-start.sh config/server.properties 2>&1 > kafka.log & echo "kafka started with pid: $!" &


exit