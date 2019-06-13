# ddos-detect
Attempt to detect DDOS attacks by log events

[![Build Status](https://travis-ci.org/seanstory/ddos-detect.svg?branch=master)](https://travis-ci.org/seanstory/ddos-detect)


### Local Development

##### Kafka

You can start a local kafka cluster by running `./src/test/resources/install_kafka.sh target/`

This will prompt you to answer if you'd like to start zookeeper and kafka, then download, install,
and start (optionally) kafka and zookeeper - forwarding the logs inside the install directory, and 
printing out the PIDs at which your servers are running.

You can then create a topic like: `./target/kafka*/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic test`

