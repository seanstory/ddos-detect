package com.sstory.ddos;

import org.apache.spark.api.java.Optional;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class IndividualIPLimitStrategy implements DdosDetectionStrategy, Serializable {

    private static Logger logger = LoggerFactory.getLogger(IndividualIPLimitStrategy.class);

    private Long maxRequestsAllowed;

    public IndividualIPLimitStrategy(Long limit){
        maxRequestsAllowed = limit;
    }

    @Override
    public JavaDStream<String> detectDdos(JavaDStream<SparkConsumer.LogEvent> logEvents) {
        JavaPairDStream<String, Long> countsByIpAddress = logEvents.map(logEvent -> {
            logger.trace("Saw IP: '{}' for log event: '{}'", logEvent.getIpAddress(), logEvent);
            return logEvent.getIpAddress();
        }).countByValue();
        JavaPairDStream<String, Long> runningCounts = countsByIpAddress.updateStateByKey((values, state) ->
            Optional.of(values.stream().mapToLong(i -> i).sum() + (state.isPresent() ? state.get() : 0L)));

        JavaPairDStream<String, Long> filtered = runningCounts.filter(pair -> pair._2 > maxRequestsAllowed);

        filtered.persist();
        filtered.foreachRDD(rdd -> {
            if(rdd.isEmpty()){
                logger.info("No DDOS threat detected... yet");
            }
        });

        return filtered.map(pair -> {
            logger.warn("Detected '{}' may be a suspicious IP, with {} requests so far", pair._1, pair._2);
            return pair._1;
        });
    }
}
