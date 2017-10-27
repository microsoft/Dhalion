package com.microsoft.dhalion.sensor;

import com.microsoft.dhalion.api.ISensor;
import com.microsoft.dhalion.metrics.ComponentMetrics;
import com.microsoft.dhalion.metrics.MetricsStats;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class SensorImpl implements ISensor {
  private final String metricName;
  protected ComponentMetrics metrics = new ComponentMetrics();
  protected Map<String, MetricsStats> stats = new HashMap<>();

  public SensorImpl(String metricName) {
    this.metricName = metricName;
  }

  @Override
  abstract public ComponentMetrics fetchMetrics();

  @Override
  public ComponentMetrics getMetrics() {
    return metrics;
  }

  @Override
  public Map<String, MetricsStats> getStats() {
    return stats;
  }

  @Override
  public Optional<MetricsStats> getStats(String component) {
    if (stats == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(stats.get(component));
  }

  @Override
  public String getMetricName() {
    return metricName;
  }
}
