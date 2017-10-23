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
import java.util.Optional;

public class MetricsState {
  //Map from component name to metrics for the component
  private HashMap<String, ComponentMetrics> metricsSnapshot = new HashMap<>();

  //Map from component name to (metric name, metric stats) for the component
  private HashMap<String, HashMap<String, MetricsStats>> stats = new HashMap<>();

  public void addMetricsAndStats(Map<String, ComponentMetrics> metrics,
                                 Map<String, MetricsStats> componentStats) {
    addMetrics(metrics);
    addStats(componentStats);
  }

  void addMetrics(Map<String, ComponentMetrics> metrics) {
    if (metrics == null) {
      return;
    }

    metrics.forEach(this::addMetrics);
  }

  private void addStats(Map<String, MetricsStats> stats) {
    if (stats == null) {
      return;
    }

    stats.forEach(this::addStats);
  }

  private void addMetrics(String component, ComponentMetrics metrics) {
    ComponentMetrics currentMetrics = metricsSnapshot.get(component);
    if (currentMetrics == null) {
      metricsSnapshot.put(component, metrics);
    } else {
      metricsSnapshot.put(component, ComponentMetrics.merge(currentMetrics, metrics));
    }
  }

  private void addStats(String component, MetricsStats inputStats) {
    HashMap<String, MetricsStats> componentStats = stats.computeIfAbsent(component, k -> new HashMap<>());

    String metric = inputStats.getMetricName();
    if (componentStats.get(inputStats.getMetricName()) != null) {
      throw new DuplicateMetricException(String.format("Metric %s already exists for component %s", metric, component));
    }

    componentStats.put(metric, inputStats);
  }

  public Map<String, ComponentMetrics> getMetrics() {
    return metricsSnapshot;
  }

  public HashMap<String, HashMap<String, MetricsStats>> getStats() {
    return stats;
  }

  public Optional<ComponentMetrics> getMetrics(String component) {
    return Optional.ofNullable(metricsSnapshot.get(component));
  }

  public MetricsStats getStats(String component, String metricName) {
    if (stats.get(component) != null) {
      return stats.get(component).get(metricName);
    }
    return null;
  }

  public void clearMetrics() {
    metricsSnapshot.clear();
  }
}
