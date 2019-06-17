package com.sstory.ddos;

import org.apache.spark.streaming.api.java.JavaDStream;

/**
 * For use with the Strategy pattern, allowing dynamic approaches to detecting a DDOS
 */
public interface DdosDetectionStrategy {

    /**
     * Taking a DStream of LogEvents, determines a DStream of suspicious IPAddresses.
     * @param logEvents - the DStream of LogEvents to be analyzed.
     * @return A DStream of IP addresses that are suspicious, and may be part of a DDOS attack.
     */
    JavaDStream<String> detectDdos(JavaDStream<SparkConsumer.LogEvent> logEvents);
}
