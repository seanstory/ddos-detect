package com.sstory.ddos;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SparkConsumer {

    private static Logger log = LoggerFactory.getLogger(SparkConsumer.class);
    private static String MASTER_MODE = "local[*]";
    private static String APP_NAME = "DDOS-Detect";
    private static int WINDOW_DURATION_SECONDS = 3;
    private static Pattern LOG_REGEX = Pattern.compile("([\\d\\.]+) - - \\[(.*?)\\] \"(.*?)\" (\\d+) (\\d+) \"-\" \"(.*?)\"");
    private static DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");
    private static int IP_INDEX = 1;
    private static int TIMESTAMP_INDEX = 2;
    private static int METHOD_INDEX = 3;
    private static int RESPONSE_CODE_INDEX = 4;
    private static int RESPONSE_SIZE_INDEX = 5;
    private static int USER_AGENT_INDEX = 6;
    private SparkConf conf;
    private JavaStreamingContext jssc;

    public SparkConsumer(String checkpointDir) {
        conf = new SparkConf().setMaster(MASTER_MODE).setAppName(APP_NAME);
        conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        conf.set("spark.streaming.kafka.consumer.cache.enabled", "false");
        jssc = new JavaStreamingContext(conf, Durations.seconds(WINDOW_DURATION_SECONDS));
        jssc.checkpoint(checkpointDir);
        jssc.sparkContext().hadoopConfiguration().set("mapreduce.fileoutputcommitter.marksuccessfuljobs", "false");
    }

    //TODO this still needs to be integrated.
    public JavaPairDStream<Integer, String> getStreamFromKafka(String kafkaUrl, String topic) {
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", kafkaUrl);
        kafkaParams.put("key.deserializer", IntegerDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "com.sstory");
        kafkaParams.put("auto.offset.reset", "earliest");
        kafkaParams.put("enable.auto.commit", false);

        JavaInputDStream<ConsumerRecord<Integer, String>> stream = KafkaUtils.createDirectStream(jssc, LocationStrategies.PreferConsistent(), ConsumerStrategies.Subscribe(new ArrayList<String>(Arrays.asList(topic)), kafkaParams));
        return stream.mapToPair( record -> new Tuple2<>(record.key(), record.value()));
    }

    public void consume(JavaPairDStream<Integer, String> inputStream, DdosDetectionStrategy ddosDetectionStrategy, String outputDir) {
        JavaPairDStream<Integer, LogEvent> eventStream = inputStream.mapValues(new ConvertLogLineFunction());
        JavaDStream<LogEvent> logEvents = eventStream.map(tuple -> tuple._2);

        JavaDStream<String> suspiciousIPs = ddosDetectionStrategy.detectDdos(logEvents);

        suspiciousIPs.dstream().saveAsTextFiles(outputDir, "out");
    }

    public void start(){
        jssc.start();
    }

    public void await() throws InterruptedException {
        jssc.awaitTermination();
    }

    public void stop(){
        jssc.stop();
    }


    public static class ConvertLogLineFunction implements Function<String, LogEvent> {
        @Override
        public LogEvent call(String logLine) throws Exception {
            Matcher matcher = LOG_REGEX.matcher(logLine);
            if (!matcher.find()) {
                return null;
            } else {
                String ipAddress = matcher.group(IP_INDEX);
                String timeStampStr = matcher.group(TIMESTAMP_INDEX);
                ZonedDateTime timeStamp = ZonedDateTime.parse(timeStampStr, TIMESTAMP_FORMAT);
                String method = matcher.group(METHOD_INDEX);
                int responseCode = Integer.parseInt(matcher.group(RESPONSE_CODE_INDEX));
                int responseSize = Integer.parseInt(matcher.group(RESPONSE_SIZE_INDEX));
                String userAgent = matcher.group(USER_AGENT_INDEX);

                return new LogEvent(ipAddress, timeStamp, method, responseCode, responseSize, userAgent);
            }

        }

    }

    public static class LogEvent {

        private String ipAddress;
        private ZonedDateTime timestamp;
        private String method;
        private int responseCode;
        private int responseSize;
        private String userAgent;

        public LogEvent(String ipAddress, ZonedDateTime timestamp, String method, int responseCode, int responseSize, String userAgent){
            this.ipAddress = ipAddress;
            this.timestamp = timestamp;
            this.method = method;
            this.responseCode = responseCode;
            this.responseSize = responseSize;
            this.userAgent = userAgent;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public ZonedDateTime getTimestamp() {
            return timestamp;
        }

        public String getMethod() {
            return method;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public int getResponseSize() {
            return responseSize;
        }

        public String getUserAgent() {
            return userAgent;
        }

        @Override
        public String toString(){
            return "IP: "+ipAddress +"\n" +
                    "timestamp: "+timestamp+"\n" +
                    "method: "+method+"\n" +
                    "responseCode: "+responseCode+"\n" +
                    "responseSize: "+responseSize+"\n" +
                    "userAgent: "+userAgent;
        }

    }
}
