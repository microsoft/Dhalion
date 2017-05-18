/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.metrics;

import java.util.HashMap;

/**
 * An {@link InstanceMetricsData} holds metrics information for a specific instance.
 */
public class InstanceMetricsData {
  // id of the instance
  protected String name;

  // Time when instance data was collected
  protected long timestampMillis;

  // Duration over which the metrics were collected
  protected int durationSec;

  // a map of metric name and its value
  private HashMap<String, Double> metrics = new HashMap<>();

  public InstanceMetricsData(String name) {
    this.name = name;
    this.timestampMillis = System.currentTimeMillis();
    this.durationSec = 0;
  }

  public void addMetric(String name, double value) {
    if (metrics.containsKey(name)) {
      throw new IllegalArgumentException("Metric exists: " + name);
    }
    metrics.put(name, value);
  }

  public Double getMetric(String name) {
    return metrics.get(name);
  }

  public int getMetricIntValue(String name) {
    return metrics.get(name).intValue();
  }

  public String getName() {
    return name;
  }

  public long getTimestamp() {
    return timestampMillis;
  }

  public int getDurationSec() {
    return durationSec;
  }

  public boolean hasMetricAboveLimit(String metricName, double limit) {
    return metrics.get(metricName) > limit;
  }

  /**
   * Merges instance metrics in two different objects into one. Input objects are not modified. It
   * is assumed that the two input data sets belong to the same instance
   *
   * @return A new {@link InstanceMetricsData} object
   */
  public static InstanceMetricsData merge(InstanceMetricsData data1, InstanceMetricsData data2) {
    InstanceMetricsData mergedData = new InstanceMetricsData(data1.getName());
    for (String metric : data1.metrics.keySet()) {
      mergedData.addMetric(metric, data1.getMetric(metric));
    }
    for (String metric : data2.metrics.keySet()) {
      mergedData.addMetric(metric, data2.getMetric(metric));
    }

    return mergedData;
  }
}
