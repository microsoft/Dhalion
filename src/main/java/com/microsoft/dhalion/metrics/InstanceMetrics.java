/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.metrics;

import com.microsoft.dhalion.common.InstanceInfo;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * An {@link InstanceMetrics} holds metric information of a specific metric for instance of a component.
 */
public class InstanceMetrics extends InstanceInfo {
  // id of the component
  private final String metricName;

  //metric values at different times
  private Map<Instant, Double> metrics = new HashMap<>();

  public InstanceMetrics(String componentName, String instanceName, String metricName) {
    super(componentName, instanceName);
    this.metricName = metricName;
  }

  public void addValues(Map<Instant, Double> values) {
    metrics.putAll(values);
  }

  /**
   * Adds a metric and its value for the instance. This is a shorthand method for
   * {@link InstanceMetrics#addValues} method. The assumption is that the metric will have only one
   * value.
   *
   * @param value metric value
   */
  public void addValue(double value) {
    metrics.put(Instant.now(), value);
  }

  public void addValue(Instant time, double value) {
    metrics.put(time, value);
  }

  public Map<Instant, Double> getValues() {
    return metrics;
  }

  public String getMetricName() {
    return metricName;
  }

  public Double getValueSum() {
    return metrics.values().stream().mapToDouble(x -> x.doubleValue()).sum();
  }

  public boolean hasValueAboveLimit(double limit) {
    return metrics.values().stream().anyMatch(x -> x > limit);
  }

  @Override
  public String toString() {
    return "InstanceMetrics{" +
        "metricName='" + metricName + '\'' +
        ", metrics=" + metrics +
        ", componentName='" + componentName + '\'' +
        ", instanceName='" + instanceName + '\'' +
        '}';
  }
}
