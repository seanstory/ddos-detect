import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import static ch.qos.logback.classic.Level.*

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d %highlight(%-5level) [%thread] %logger{36} - %msg%n"
    }
}

logger("org.apache.spark.streaming", WARN)
logger("org.apache.spark.rdd", WARN)
logger("org.apache.spark.storage", WARN)
logger("org.apache.spark.scheduler", WARN)
logger("org.apache.kafka.clients.consumer.ConsumerConfig", WARN)

root(INFO, ["CONSOLE"])
