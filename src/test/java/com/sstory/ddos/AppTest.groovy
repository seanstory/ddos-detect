package com.sstory.ddos

import spock.lang.Ignore
import spock.lang.Specification

/*
To run this test, follow the instructions in the readme to start a local kafka. Then you can run:

./target/kafka_2.12-2.2.0/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning
 */
@Ignore
class AppTest extends Specification {

    def "is it working?"(){
        setup:
        def sampleFile = "src/test/resources/data/apapche-access-log.txt"
        def kafkaUrl = "localhost:9092"
        def kafkaTopic = "test"
        def output = "target/output"
        def checkpoint = "target/checkpoint"
        def limit = 40

        def outputDir = new File(output)
        if(outputDir.exists()){
            outputDir.deleteDir()
        }
        outputDir.mkdirs()

        def checkpointDir = new File(checkpoint)
        if(checkpointDir.exists()){
            checkpointDir.deleteDir()
        }
        checkpointDir.mkdirs()

        when:
        App.main([sampleFile, kafkaUrl, kafkaTopic, output, checkpoint, limit] as String[])

        then:
        noExceptionThrown()
    }
}
