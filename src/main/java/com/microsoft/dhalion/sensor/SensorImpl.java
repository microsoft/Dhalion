package com.microsoft.dhalion.sensor;

import com.microsoft.dhalion.api.ISensor;
import com.microsoft.dhalion.metrics.ComponentMetrics;

public abstract class SensorImpl implements ISensor {
  private final String metricName;
  protected ComponentMetrics metrics = new ComponentMetrics();

  public SensorImpl(String metricName) {
    this.metricName = metricName;
  }

  @Override
  abstract public ComponentMetrics fetchMetrics();

  @Override
  public ComponentMetrics readMetrics() {
    return metrics;
  }

  @Override
  public String getMetricName() {
    return metricName;
  }
}
