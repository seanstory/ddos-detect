# ddos-detect
Attempt to detect DDOS attacks by log events

[![Build Status](https://travis-ci.org/seanstory/ddos-detect.svg?branch=master)](https://travis-ci.org/seanstory/ddos-detect)

### Building
Build this project with a simple `mvn clean install`

### Local Development

##### Kafka

Clean up any previous content with:  `rm -r /tmp/kafka-logs` and `rm -r /tmp/zookeeper/`

You can start a local kafka cluster by running `./src/test/resources/install_kafka.sh target/`

This will prompt you to answer if you'd like to start zookeeper and kafka, then download, install,
and start (optionally) kafka and zookeeper - forwarding the logs inside the install directory, and 
printing out the PIDs at which your servers are running.

You can then create a topic like: `./target/kafka*/bin/kafka-topics.sh --topic test --create --replication-factor 1 --partitions 10 --zookeeper localhost:2181`

### Test it Out
After following the above instructions for setting up a local Kafka, visit `AppTest` in your IDE, uncomment the `@Ignore`
annotation, and try a canned example! The test will run for about a minute, populating your local topic, consuming from it,
and taking a naive approach to detect a DDOS in progress.

### Approach
This application has two detection strategies OOTB. 
* `IndividualIPLimitStrategy` will keep track of how many requests a given IP makes across the entire lifetime of the app. This is good naive approach, but would be problematic for long-lived services where a regular customer accrues as may requests as an attacker might make in just a few minutes.
* `WindowedIPLimitStrategy` takes a similar approach, but limits its analysis to within just a small window. This is a better approach, but still naive, especially since many DDOS attacks spoof their IP addresses.

Future work might involve creating more nuanced detection strategies - leveraging entropy formulas, looking for error code responses, or simply assessing sudden up-ticks in traffic.