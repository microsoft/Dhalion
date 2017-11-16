/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.metrics;

import com.microsoft.dhalion.common.DuplicateMetricException;
import com.microsoft.dhalion.common.InstanceInfo;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An {@link InstanceMetrics} holds metric information of a specific metric for instance of a component.
 */
public class InstanceMetrics extends InstanceInfo {
  // id of the component
  private final String metricName;

  //metric values at different times
  private Map<Instant, Double> metrics = new TreeMap<>();

  /**
   * @param componentName name/id of a component, not null
   * @param instanceName  name/id of a instance, not null
   * @param metricName    name/id of a metric, not null
   */
  public InstanceMetrics(String componentName, String instanceName, String metricName) {
    super(componentName, instanceName);
    this.metricName = metricName;
  }

  /**
   * Adds multiple instant-value pairs. The operation fails if a instant to be added already exists.
   *
   * @param values values to be added, not null
   */
  public void addValues(Map<Instant, Double> values) {
    values.entrySet().stream()
        .filter(entry -> metrics.containsKey(entry.getKey())).findAny()
        .ifPresent(x -> {
          throw new DuplicateMetricException(componentName, instanceName, metricName);
        });
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

  /**
   * Adds a instant-value pair. The operation fails if a instant to be added already exists.
   *
   * @param time  instant at which metric was recorded
   * @param value value of the metric
   */
  public void addValue(Instant time, double value) {
    if (metrics.containsKey(time)) {
      throw new DuplicateMetricException(componentName, instanceName, metricName);
    }
    metrics.put(time, value);
  }

  /**
   * Reads metric values from the other instance and adds it to this instance. The operation will fail if the instances
   * have different metricn, component or instance name. It will also fail if both instances have a value for the
   * same instant value.
   *
   * @param o other instance of {@link InstanceMetrics}, not null
   */
  public void merge(InstanceMetrics o) {
    if (!metricName.equals(o.metricName)) {
      throw new IllegalArgumentException(String.format("Metric name mismatch: %s vs %s", metricName, o.metricName));
    }
    if (!componentName.equals(o.componentName)) {
      throw new IllegalArgumentException(String.format("Component name mismatch: %s vs %s", metricName, o.metricName));
    }
    if (!instanceName.equals(o.instanceName)) {
      throw new IllegalArgumentException(String.format("Instance name mismatch: %s vs %s", metricName, o.metricName));
    }

    o.getValues().entrySet().stream()
        .filter(entry -> metrics.containsKey(entry.getKey())).findAny()
        .ifPresent(x -> {
          throw new DuplicateMetricException(componentName, instanceName, metricName);
        });

    metrics.putAll(o.metrics);
  }

  public Map<Instant, Double> getMostRecentValues(int noRecentValues) {
    Map<Instant, Double> recentValues;
    recentValues = (Map<Instant, Double>) ((TreeMap) metrics).descendingMap().keySet().stream().limit(noRecentValues)
        .collect(Collectors.toMap(Function.identity(), instant -> metrics.get(instant)));
    return recentValues;
  }

  public TreeSet<Instant> getMostRecentTimestamps(int noRecentValues) {
    TreeSet<Instant> recentTimestamps;
    recentTimestamps = new TreeSet((Collection) ((TreeMap) metrics).descendingMap().keySet().stream()
                                                                   .limit(noRecentValues).collect(Collectors.toSet()));
    return recentTimestamps;
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

  public Double getValueSum(int noRecentValues) {
    return getMostRecentValues(noRecentValues).values().stream().mapToDouble(x -> x.doubleValue()).sum();
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
