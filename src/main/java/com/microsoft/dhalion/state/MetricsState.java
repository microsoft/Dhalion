/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.state;

import com.microsoft.dhalion.common.DuplicateMetricException;
import com.microsoft.dhalion.metrics.ComponentMetrics;
import com.microsoft.dhalion.metrics.MetricsStats;

import java.util.HashMap;
import java.util.Map;

public class MetricsState {
  private ComponentMetrics metricsSnapshot = new ComponentMetrics();

  //Map from component name to (metric name, metric stats) for the component
  private HashMap<String, HashMap<String, MetricsStats>> stats = new HashMap<>();

  public void addMetricsAndStats(ComponentMetrics metrics,
                                 Map<String, MetricsStats> componentStats) {
    addMetrics(metrics);
    addStats(componentStats);
  }

  void addMetrics(ComponentMetrics metrics) {
    metricsSnapshot = ComponentMetrics.merge(metricsSnapshot, metrics);
  }

  private void addStats(Map<String, MetricsStats> stats) {
    if (stats == null) {
      return;
    }

    stats.forEach(this::addStats);
  }

  private void addStats(String component, MetricsStats inputStats) {
    HashMap<String, MetricsStats> componentStats = stats.computeIfAbsent(component, k -> new HashMap<>());

    String metric = inputStats.getMetricName();
    if (componentStats.get(inputStats.getMetricName()) != null) {
      throw new DuplicateMetricException(component, "", metric);
    }

    componentStats.put(metric, inputStats);
  }

  public ComponentMetrics getMetrics() {
    return metricsSnapshot;
  }

  public HashMap<String, HashMap<String, MetricsStats>> getStats() {
    return stats;
  }

  public MetricsStats getStats(String component, String metricName) {
    if (stats.get(component) != null) {
      return stats.get(component).get(metricName);
    }
    return null;
  }

  public void clearMetrics() {
    metricsSnapshot = new ComponentMetrics();
  }
}