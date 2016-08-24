import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.FileAppender
import ch.qos.logback.classic.filter.ThresholdFilter

def bySecond = timestamp("yyyyMMdd'T'HHmmss")

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = '%d{HH:mm:ss.SSS} [%thread] %-5level %logger{40} - %msg%n'
    }
    filter(ThresholdFilter) {
        level = DEBUG
    }

}

appender("TEST_RESULTS", FileAppender) {
    file = "results/result-${bySecond}.txt"
    encoder(PatternLayoutEncoder) {
        pattern = '%msg%n'
    }
    filter(ThresholdFilter) {
        level = WARN
    }
}

root(DEBUG, ["CONSOLE"])
logger("org.springframework", INFO)
logger("com.hazelcast", INFO)
logger("org.age.hz", INFO, ["CONSOLE"])
logger("org.age.hz", WARN, ["TEST_RESULTS"])
