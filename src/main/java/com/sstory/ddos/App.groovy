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
        if(args.length != 3){
            throw new IllegalArgumentException("Expected args: <inputLogFile> <kafkaUrl> <kafkaTopic>")
        }
        File logFile = new File(args[0])
        String kafkaUrl = args[1]
        String kafkaTopic = args[2]
        if(!logFile.exists() || !logFile.isFile() || !logFile.canRead()){
            throw new IllegalStateException("Expected '${logFile}' to exist, be a single file, and be readable!")
        }
        FileProducer producer = new FileProducer(kafkaUrl, kafkaTopic)
        producer.produceFrom(logFile)

        //TODO finish

    }
}
