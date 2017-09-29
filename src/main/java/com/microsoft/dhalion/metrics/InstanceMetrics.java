/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.metrics;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * An {@link InstanceMetrics} holds metrics information for a specific instance.
 */
public class InstanceMetrics {
  // id of the instance
  protected final String instanceName;

  // a map of metric name and its values
  private Map<String, Map<Instant, Double>> metrics = new HashMap<>();

  public InstanceMetrics(String instanceName) {
    this(instanceName, null, 0.0);
  }

  public InstanceMetrics(String instanceName, String metricName, double value) {
    this.instanceName = instanceName;
    if (metricName != null) {
      addMetric(metricName, value);
    }
  }

  public void addMetric(String name, Map<Instant, Double> values) {
    if (metrics.containsKey(name)) {
      throw new IllegalArgumentException("Metric exists: " + name);
    }
    Map<Instant, Double> metricValues = new HashMap<>();
    metricValues.putAll(values);
    metrics.put(name, metricValues);
  }

  /**
   * Adds a metric and its value for the instance. This is a shorthand method for
   * {@link InstanceMetrics#addMetric(String, Map)} method. The assumption is that the metric will
   * have only one value.
   *
   * @param metricName metric name
   * @param value      metric value
   */
  public void addMetric(String metricName, double value) {
    Map<Instant, Double> metricValues = new HashMap<>();
    metricValues.put(Instant.now(), value);
    addMetric(metricName, metricValues);
  }

  public Map<String, Map<Instant, Double>> getMetrics() {
    return metrics;
  }

  public Double getMetricValueSum(String metricName) {
    Map<Instant, Double> values = getMetrics().get(metricName);
    if (values == null || values.isEmpty()) {
      return null;
    }

    return values.values().stream().mapToDouble(x -> x.doubleValue()).sum();
  }

  public String getName() {
    return instanceName;
  }

  public boolean hasMetricAboveLimit(String metricName, double limit) {
    Map<Instant, Double> values = metrics.get(metricName);
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
      mergedData.addMetric(metric, data1.getMetrics().get(metric));
    }
    for (String metric : data2.metrics.keySet()) {
      mergedData.addMetric(metric, data2.getMetrics().get(metric));
    }

    return mergedData;
  }

  @Override
  public String toString() {
    return "InstanceMetrics{" +
        "name='" + instanceName +
        ", metrics=" + metrics +
        '}';
  }
}
