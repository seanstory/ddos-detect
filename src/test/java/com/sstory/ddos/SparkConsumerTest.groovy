package com.sstory.ddos

import org.apache.spark.api.java.function.PairFunction
import scala.Tuple2
import spock.lang.Specification

class SparkConsumerTest extends Specification {

    def "test converting log lines to objects"(){
        setup:
        String logLine = '209.112.9.34 - - [25/May/2015:23:11:15 +0000] "GET / HTTP/1.0" 200 3557 "-" "Mozilla/3.01 (compatible;)"'
        def function = new SparkConsumer.ConvertLogLineFunction()

        when:
        SparkConsumer.LogEvent logEvent = function.call(logLine)

        then:
        logEvent.ipAddress == '209.112.9.34'
        logEvent.timestamp.format(SparkConsumer.TIMESTAMP_FORMAT) == '25/May/2015:23:11:15 +0000'
        logEvent.method == "GET / HTTP/1.0"
        logEvent.responseCode == 200
        logEvent.responseSize == 3557
        logEvent.userAgent == "Mozilla/3.01 (compatible;)"
    }

    def "test spark without kafka"(){
        setup:
        def consumer = new SparkConsumer()

        def sc = consumer.jssc.sparkContext()
        def rdd = sc.parallelize([
                '200.4.91.190 - - [25/May/2015:23:11:15 +0000] "GET / HTTP/1.0" 200 3557 "-" "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)"',
                '209.112.63.162 - - [25/May/2015:23:11:15 +0000] "GET / HTTP/1.0" 200 3557 "-" "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; {1C69E7AA-C14E-200E-5A77-8EAB2D667A07})"',
                '209.112.9.34 - - [25/May/2015:23:11:15 +0000] "GET / HTTP/1.0" 200 3557 "-" "Mozilla/3.01 (compatible;)"'
        ])

        def queue = [] as Queue
        def dstream = consumer.jssc.queueStream(queue, false, rdd)
        def pairDStream = dstream.mapToPair(new SamplePairFunction())

        when:
        consumer.consume(pairDStream)
        consumer.jssc.start()
        queue.add(rdd)
        consumer.jssc.awaitTerminationOrTimeout(2_000) //just to show it's running

        then:
        noExceptionThrown()
    }

    static class SamplePairFunction implements  PairFunction<String, Integer, String> {
        @Override
        Tuple2<Integer, String> call(String str) throws Exception {
            new Tuple2<Integer, String>(0, str)
        }
    }
}
