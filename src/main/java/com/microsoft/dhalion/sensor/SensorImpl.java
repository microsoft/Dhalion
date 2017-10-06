package com.microsoft.dhalion.sensor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.microsoft.dhalion.api.ISensor;
import com.microsoft.dhalion.metrics.ComponentMetrics;
import com.microsoft.dhalion.metrics.MetricsStats;
import com.microsoft.dhalion.state.State;

public class SensorImpl implements ISensor {


  private final String metricName;
  private State stateSnapshot;

  public SensorImpl(String metricName) {
    this.metricName = metricName;
  }


  @Override
  public void initialize(State stateSnapshot) {
    this.stateSnapshot = stateSnapshot;
  }

  @Override
  public Map<String, ComponentMetrics> get(String... components) {
    Set<String> boltNameFilter = new HashSet<>();
    if (components.length > 0) {
      boltNameFilter.addAll(Arrays.asList(components));
    }

    Map<String, ComponentMetrics> result = new HashMap<>();
    for (Map.Entry<String, ComponentMetrics> entry : this.stateSnapshot.getState().entrySet()) {
      if (boltNameFilter.contains(entry.getKey())) {
        result.put(entry.getKey(), entry.getValue().getComponentMetric(this.metricName));
      }
    }
    return result;
  }

  @Override
  public MetricsStats getStats(String component) {
    String metricName = this.getMetricName();
    MetricsStats result = this.stateSnapshot.getStats(component, metricName);
    return result;
  }

  public String getMetricName() {
    return metricName;
  }

}
