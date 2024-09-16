package org.example.log;

import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

public class MetricsLogger {
    private static final Logger metricsLogger = LoggerFactory.getLogger("metrics");

    public static void startLogging(MetricRegistry metricRegistry) {
        Slf4jReporter reporter = Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(metricsLogger)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();

        // Schedule the logging to occur
        reporter.start(200, TimeUnit.MILLISECONDS);
    }
}
