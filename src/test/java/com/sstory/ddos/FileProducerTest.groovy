package com.sstory.ddos

import java.util.concurrent.Future
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import spock.lang.Specification

class FileProducerTest extends Specification {

    def "test adding lines to kafka"(){
        setup:
        String topic = "test"
        KafkaProducer<Integer, String> backingProducer = Mock(KafkaProducer)

        Future<RecordMetadata> future = Mock(Future)
        def producer = new FileProducer(backingProducer, topic)
        def input = new File("src/test/resources/data/small-file.txt")

        when:
        producer.produceFrom(input)

        then:
        1 * backingProducer.send(new ProducerRecord<>(topic, 0, "foo")) >> future
        1 * backingProducer.send(new ProducerRecord<>(topic, 1, "bar")) >> future
        1 * backingProducer.send(new ProducerRecord<>(topic, 2, "baz")) >> future
    }
}
