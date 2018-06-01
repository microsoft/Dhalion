package com.microsoft.dhalion.sensors;

import com.microsoft.dhalion.api.MetricsProvider;
import com.microsoft.dhalion.conf.Config;
import com.microsoft.dhalion.conf.PolicyConfig;
import com.microsoft.dhalion.core.Measurement;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DirectSensor extends BaseSensor {
  private static final Logger LOG = Logger.getLogger(DirectSensor.class.getName());
  private static final Duration DEFAULT_METRIC_DURATION = Duration.ofSeconds(60);

  private Collection<String> components;

  @Inject
  public DirectSensor(PolicyConfig policyConf,
             Config sysConf,
             String metricName,
             MetricsProvider metricsProvider) {
    super(policyConf, metricName, metricsProvider);
    this.components = Arrays.asList(sysConf.get("component.names").toString().split(","));
  }

  public Collection<String> getComponents() {
    return components;
  }

  /**
   * Returns the duration for which the metrics need to be collected
   *
   * @return duration in seconds
   */
  protected synchronized Duration getDuration() {

    if (context.previousCheckpoint() == Instant.MIN) {
      return DEFAULT_METRIC_DURATION;
    } else {
      return Duration.ofSeconds((context.checkpoint().toEpochMilli() - context.previousCheckpoint().toEpochMilli())
                                    / 1000);
    }
  }

  public Collection<Measurement> fetch() {
    Instant startTime = context.checkpoint();
    Duration duration = getDuration();
    Collection<String> metrics = getMetricTypes();
    LOG.info(String.format("Trying to fetch %s @ %s for %s duration", getMetricTypes(), startTime, duration));

    try {
      Collection<Measurement> measurements
          = metricsProvider.getMeasurements(startTime, duration, metrics, getComponents());
      if (LOG.isLoggable(Level.FINE)) {
        measurements.stream().map(Object::toString).forEach(LOG::finest);
      }
      return measurements;
    } catch (Exception e) {
      LOG.log(Level.INFO, "Missing stat", e);
      return Collections.emptyList();
    }
  }
}
