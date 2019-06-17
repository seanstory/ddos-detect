package com.sstory.ddos;

import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class WindowedIPLimitStrategy implements DdosDetectionStrategy, Serializable {

    private static Logger logger = LoggerFactory.getLogger(com.sstory.ddos.IndividualIPLimitStrategy.class);

    private Long maxRequestsAllowed;

    public WindowedIPLimitStrategy(Long limit){
        maxRequestsAllowed = limit;
    }

    @Override
    public JavaDStream<String> detectDdos(JavaDStream<SparkConsumer.LogEvent> logEvents) {
        JavaPairDStream<String, Long> countsByIpAddress = logEvents.map(logEvent -> {
            logger.trace("Saw IP: '{}' for log event: '{}'", logEvent.getIpAddress(), logEvent);
            return logEvent.getIpAddress();
        }).countByValue();

        JavaPairDStream<String, Long> runningCounts = countsByIpAddress.reduceByKeyAndWindow((i1, i2) -> i1 + i2, Durations.seconds(30), Durations.seconds(9));

        JavaPairDStream<String, Long> filtered = runningCounts.filter(pair -> pair._2 > maxRequestsAllowed);

        filtered.persist();
        filtered.foreachRDD(rdd -> {
            if(rdd.isEmpty()){
                logger.info("No DDOS threat detected... yet");
            } else {
                logger.info("We may be under attack!");
            }
        });

        return filtered.map(pair -> {
            logger.warn("Detected '{}' may be a suspicious IP", pair._1);
            return pair._1;
        });
    }

}
