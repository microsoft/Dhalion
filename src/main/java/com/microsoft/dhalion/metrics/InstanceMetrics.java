/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.metrics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * An {@link InstanceMetrics} holds metrics information for a specific instance.
 */
public class InstanceMetrics {
  // id of the instance
  protected final String name;

  // a map of metric name and its values
  private Map<String, Map<Long, Double>> metrics = new HashMap<>();

  public InstanceMetrics(String name) {
    this.name = name;
  }

  public void addMetric(String name, Map<Long, Double> values) {
    if (metrics.containsKey(name)) {
      throw new IllegalArgumentException("Metric exists: " + name);
    }
    Map<Long, Double> metricValues = new HashMap<>();
    metricValues.putAll(values);
    metrics.put(name, metricValues);
  }

  /**
   * Adds a metric and its value for the instance. This is a shorthand method for
   * {@link InstanceMetrics#addMetric(String, Map)} method. The assumption is that the metric will
   * have only one value.
   *
   * @param name metric name
   * @param value metric value
   */
  public void addMetric(String name, double value) {
    Map<Long, Double> metricValues = new HashMap<>();
    metricValues.put(System.currentTimeMillis(), value);
    addMetric(name, metricValues);
  }

  public Collection<String> getMetrics() {
    return metrics.keySet();
  }

  /**
   * @return all known values for the given metric name for this instance
   */
  public Map<Long, Double> getMetricValues(String metricName) {
    return metrics.get(metricName);
  }

  /**
   * @return the only known value of this metric. Use
   * {@link InstanceMetrics#getMetricValues(String)} when more than one value is known.
   */
  public Double getMetricValue(String metricName) {
    Map<Long, Double> values = getMetricValues(metricName);
    if (values == null || values.isEmpty()) {
      return null;
    }

    if (values.size() > 1) {
      throw new IllegalStateException();
    }
    return values.values().iterator().next();
  }

  public String getName() {
    return name;
  }

  public boolean hasMetricAboveLimit(String metricName, double limit) {
    Map<Long, Double> values = metrics.get(metricName);
    if (values == null) {
      return false;
    }

    return values.values().stream().anyMatch(x -> x > limit);
  }

  /**
   * Merges instance metrics in two different objects into one. Input objects are not modified. It
   * is assumed that the two input data sets belong to the same instance. It is also assumed that
   * the two {@link InstanceMetrics} objects to be merged do not contain values for the same
   * metric.
   *
   * @return A new {@link InstanceMetrics} object
   */
  public static InstanceMetrics merge(InstanceMetrics data1, InstanceMetrics data2) {
    InstanceMetrics mergedData = new InstanceMetrics(data1.getName());
    for (String metric : data1.metrics.keySet()) {
      mergedData.addMetric(metric, data1.getMetricValues(metric));
    }
    for (String metric : data2.metrics.keySet()) {
      mergedData.addMetric(metric, data2.getMetricValues(metric));
    }

    return mergedData;
  }
}
