package com.microsoft.dhalion.sensors;

import com.microsoft.dhalion.api.ISensor;
import com.microsoft.dhalion.api.MetricsProvider;
import com.microsoft.dhalion.conf.Config;
import com.microsoft.dhalion.conf.ConfigName;
import com.microsoft.dhalion.core.Measurement;
import com.microsoft.dhalion.policy.PoliciesExecutor.ExecutionContext;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Basic Sensor consumes metrics from the metrics provider and returns them directly in their current form.
 * It can be used when the metrics returned by the metrics provider do not require any further manipulation before
 * being consumed by the detectors. Every time the Basic Sensor is invoked it returns metrics that correspond to the
 * time window between the timestamp of the previous policy invocation and the current timestamp.
 * <p>
 * The Basic Sensor takes as input a metric name, the metrics provider, and a set of components.
 * It collects the values of the given metric for these components.
 */
public class BasicSensor implements ISensor {
  private static final Logger LOG = Logger.getLogger(BasicSensor.class.getName());
  private static final Duration DEFAULT_METRIC_DURATION = Duration.ofSeconds(60);

  private final String metricName;
  private final Collection<String> components;

  protected ExecutionContext context;
  protected final MetricsProvider metricsProvider;

  public BasicSensor(Config sysconfig, String metricName, MetricsProvider metricsProvider) {
    this(sysconfig, metricName, metricsProvider, null);
  }

  public BasicSensor(Config policyConf,
                     String metricName,
                     MetricsProvider metricsProvider,
                     Collection<String> components) {
    this.metricName = metricName;
    this.metricsProvider = metricsProvider;

    if (components != null) {
      this.components = components;
    } else {
      Object result = policyConf.get(ConfigName.CONF_COMPONENT_NAMES);
      if (result != null) {
        this.components = Arrays.asList(result.toString().split(","));
      } else {
        this.components = Collections.emptyList();
      }
    }
  }

  @Override
  public void initialize(ExecutionContext context) {
    this.context = context;
  }

  @Override
  public Collection<String> getMetricTypes() {
    return Collections.singletonList(metricName);
  }

  /**
   * Fetches the required metrics
   *
   * @return a Collection of Measurements
   */
  @Override
  public Collection<Measurement> fetch() {
    Instant startTime = context.checkpoint();
    Duration duration = getDuration();
    Collection<String> metrics = getMetricTypes();
    LOG.fine(String.format("Trying to fetch %s @ %s for %s duration", getMetricTypes(), startTime, duration));

    Collection<Measurement> measurements
        = metricsProvider.getMeasurements(startTime, duration, metrics, getComponents());
    if (LOG.isLoggable(Level.FINEST)) {
      measurements.stream().map(Object::toString).forEach(LOG::finest);
    }
    return measurements;
  }

  protected Collection<String> getComponents() {
    return components;
  }

  /**
   * Returns the duration for which the metrics need to be collected
   *
   * @return duration in seconds
   */
  protected synchronized Duration getDuration() {
    if (context.previousCheckpoint() == null || context.previousCheckpoint().equals(Instant.MIN)) {
      return DEFAULT_METRIC_DURATION;
    } else {
      return Duration.between(context.previousCheckpoint(), context.checkpoint());
    }
  }
}
