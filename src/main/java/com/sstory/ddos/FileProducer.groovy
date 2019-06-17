package com.sstory.ddos

import groovy.util.logging.Slf4j
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

@Slf4j
class FileProducer {
    private KafkaProducer<Integer, String> producer
    private String topic

    FileProducer(String serverUrl, String topic){
        Properties properties = new Properties()
        properties.put("bootstrap.servers", serverUrl)
        properties.put("client.id", this.class.simpleName)
        properties.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer")
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        producer = new KafkaProducer<>(properties)
        this.topic = topic
    }

    private FileProducer(KafkaProducer<Integer, String> producer, String topic){
        this.producer = producer
        this.topic = topic
    }

    void produceFrom(File file){
        BufferedReader reader = file.newReader("UTF-8")
        int lineNum = 0
        reader.eachLine { line ->
            log.info("Sending line number {} to kafka topic: {}", lineNum, topic)
            producer.send(new ProducerRecord<>(topic, lineNum, line)).get()
            lineNum++
        }
    }
}
