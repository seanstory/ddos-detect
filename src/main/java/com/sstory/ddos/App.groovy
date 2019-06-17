package com.sstory.ddos

import groovy.util.logging.Slf4j

/**
 * Hello world!
 */
@Slf4j
class App
{
    static void main( String[] args )
    {
        log.info("Parsing args: {}", args)
        if(args.length != 6){
            throw new IllegalArgumentException("Expected args: <inputLogFile> <kafkaUrl> <kafkaTopic> <outputDir> <checkpointDir> <limit>")
        }
        File logFile = new File(args[0])
        String kafkaUrl = args[1]
        String kafkaTopic = args[2]
        File outputDir = new File(args[3])
        File checkpointDir = new File(args[4])
        Long limit = Long.valueOf(args[5])
        if(!logFile.exists() || !logFile.isFile() || !logFile.canRead()){
            throw new IllegalStateException("Expected '${logFile}' to exist, be a single file, and be readable!")
        }

        log.info("Setting up Spark workflows")
        def consumer = new SparkConsumer(checkpointDir.absolutePath)
        def input = consumer.getStreamFromKafka(kafkaUrl, kafkaTopic)
        consumer.consume(input, new WindowedIPLimitStrategy(limit), new File(outputDir, "out").absolutePath)
        log.info("Starting the consumer")
        consumer.start()

        log.info("Initializing the producer")
        FileProducer producer = new FileProducer(kafkaUrl, kafkaTopic)
        producer.produceFrom(logFile)

        sleep(5_000) //finish retrieving from kafka. In a real scenario, this would be long-lived
        consumer.stop()
    }
}
