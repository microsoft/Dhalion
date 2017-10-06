package com.microsoft.dhalion.state;

/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */


import java.util.HashMap;
import java.util.Map;

import com.microsoft.dhalion.metrics.ComponentMetrics;
import com.microsoft.dhalion.metrics.MetricsStats;

public class State {

  //Map from component name to metrics for the component
  HashMap<String, ComponentMetrics> metricsSnapshot;

  //Map from component name to (metric name, metric stats) for the component
  HashMap<String, HashMap<String, MetricsStats>> stats;

  public void initialize() {
    metricsSnapshot = new HashMap<>();
    stats = new HashMap<>();
  }

  public void addToState(Map<String, ComponentMetrics> metrics) {
    if (metrics != null) {
      for (Map.Entry<String, ComponentMetrics> entry : metrics.entrySet()) {
        addToState(entry.getKey(), entry.getValue());
      }

    }
  }

  public void addToState(Map<String, ComponentMetrics> metrics,
                                 Map<String, MetricsStats> componentStats,
                                 String metricName) {
    if (metrics != null) {
      for (Map.Entry<String, ComponentMetrics> entry : metrics.entrySet()) {
        addToState(entry.getKey(), entry.getValue());
      }
    }
    if (componentStats != null) {
      for (Map.Entry<String, MetricsStats> entry : componentStats.entrySet()) {
        addToStateStats(entry.getKey(), metricName, entry.getValue());
      }
    }
  }

  public void addToState(String component, ComponentMetrics metrics) {
    ComponentMetrics currentMetrics = metricsSnapshot.get(component);
    if (currentMetrics == null) {
      metricsSnapshot.put(component, metrics);
    } else {
      metricsSnapshot.put(component, ComponentMetrics.merge(currentMetrics, metrics));
    }
  }

  public void addToStateStats(String component, String metric, MetricsStats inputStats) {
    HashMap<String, MetricsStats> currentStats = stats.get(component);
    if (currentStats == null) {
      currentStats = new HashMap<>();
    }
    currentStats.put(metric, inputStats);
    stats.put(component, currentStats);
  }

  public HashMap<String, ComponentMetrics> getState() {
    return metricsSnapshot;
  }

  public ComponentMetrics getComponentState(String component) {
    return metricsSnapshot.get(component);
  }

  public MetricsStats getStats(String component, String metricName) {
    if (stats.get(component) != null) {
      return stats.get(component).get(metricName);
    }
    return null;
  }

  public void clearStateSnapshot() {
    metricsSnapshot.clear();
  }

}
