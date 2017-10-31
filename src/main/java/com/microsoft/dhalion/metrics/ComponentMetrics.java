/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.metrics;

import com.microsoft.dhalion.common.DuplicateMetricException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * {@link ComponentMetrics} is a collection of {@link InstanceMetrics} objects organized in a 2D space. This class holds
 * metric information for all instances of all components. The dimensions are metric name and component name. This
 * class provides methods to filter {@link InstanceMetrics} objects by along either of the two dimensions.
 */
public class ComponentMetrics {
  //Map for the component name dimension
  private HashMap<String, Set<InstanceMetricWrapper>> componentDim = new HashMap<>();

  //Map for the metric name dimension
  private HashMap<String, Set<InstanceMetricWrapper>> metricsDim = new HashMap<>();

  //Set of all metrics managed by this object
  private Set<InstanceMetricWrapper> allMetric = new HashSet<>();

  public synchronized void addAll(Collection<InstanceMetrics> metrics) {
    metrics.forEach(this::add);
  }

  public synchronized void add(InstanceMetrics metric) {
    InstanceMetricWrapper wrappedMetric = new InstanceMetricWrapper(metric);

    if (allMetric.contains(wrappedMetric)) {
      throw new DuplicateMetricException(metric.getComponentName(), metric.getInstanceName(), metric.getMetricName());
    }

    allMetric.add(wrappedMetric);
    componentDim.computeIfAbsent(metric.getComponentName(), k -> new HashSet<>()).add(wrappedMetric);
    metricsDim.computeIfAbsent(metric.getMetricName(), k -> new HashSet<>()).add(wrappedMetric);
  }

  public void addMetric(String component, String instance, String metricName, double value) {
    InstanceMetrics metric = new InstanceMetrics(component, instance, metricName);
    metric.addValue(value);
    add(metric);
  }

  public void addMetric(String component, String instance, String metricName, Instant time, double value) {
    InstanceMetrics metric = new InstanceMetrics(component, instance, metricName);
    metric.addValue(time, value);
    add(metric);
  }

  /**
   * @param componentName component name to be used for filtering metrics
   * @return a new {@link ComponentMetrics} instance containing all {@link InstanceMetrics}s belonging to {@code
   * componentName} only.
   */
  public ComponentMetrics filterByComponent(String componentName) {
    final ComponentMetrics result = new ComponentMetrics();
    Set<InstanceMetricWrapper> metrics = componentDim.get(componentName);
    if (metrics != null) {
      metrics.forEach(wrapper -> result.add(wrapper.metric));
    }

    return result;
  }

  /**
   * @param metricName metric name to be used for filtering metrics
   * @return a new {@link ComponentMetrics} instance containing all {@link InstanceMetrics}s belonging to {@code
   * metricName} only.
   */
  public ComponentMetrics filterByMetric(String metricName) {
    final ComponentMetrics result = new ComponentMetrics();
    Set<InstanceMetricWrapper> metrics = metricsDim.get(metricName);
    if (metrics != null) {
      metrics.forEach(wrapper -> result.add(wrapper.metric));
    }

    return result;
  }

  /**
   * @param componentName component name to be used for filtering metrics
   * @param instanceName  instance name to be used for filtering metrics
   * @return a new {@link ComponentMetrics} instance containing all {@link InstanceMetrics}s belonging to {@code
   * componentName/instanceName} only.
   */
  public ComponentMetrics filterByInstance(String componentName, String instanceName) {
    final ComponentMetrics result = new ComponentMetrics();
    Collection<InstanceMetrics> metrics = filterByComponent(componentName).getMetrics();
    metrics.stream()
        .filter(metric -> metric.getInstanceName().equals(instanceName))
        .forEach(result::add);

    return result;
  }

  /**
   * @param instanceName instance name to be used for filtering metrics
   * @return a new {@link ComponentMetrics} instance containing all {@link InstanceMetrics}s belonging to {@code
   * componentName/instanceName} only.
   */
  public ComponentMetrics filterByInstance(String instanceName) {
    final ComponentMetrics result = new ComponentMetrics();
    allMetric.stream()
        .map(wrapper -> wrapper.metric)
        .filter(metric -> metric.getInstanceName().equals(instanceName))
        .forEach(result::add);

    return result;
  }

  /**
   * @param componentName name of the component
   * @param instanceName  name of the instance
   * @param metricName    name of the metric
   * @return a unique {@link InstanceMetrics} if exists
   */
  public Optional<InstanceMetrics> getMetrics(String componentName, String instanceName, String metricName) {
    Collection<InstanceMetrics> metrics =
        filterByInstance(componentName, instanceName).filterByMetric(metricName).getMetrics();
    if (metrics.size() > 1) {
      throw new DuplicateMetricException(componentName, instanceName, metricName);
    }

    if (metrics.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(metrics.iterator().next());
  }

  /**
   * @return all {@link InstanceMetrics} managed by this {@link ComponentMetrics} object
   */
  public Collection<InstanceMetrics> getMetrics() {
    final Collection<InstanceMetrics> result = new ArrayList<>();
    allMetric.forEach(wrapper -> result.add(wrapper.metric));
    return result;
  }

  /**
   * @return count of metrics
   */
  public int size() {
    return allMetric.size();
  }

  /**
   * @return true if no {@link InstanceMetrics} are present
   */
  public boolean isEmpty() {
    return size() == 0;
  }

  /**
   * @return returns the only {@link InstanceMetrics} managed by this {@link ComponentMetrics}
   */
  public Optional<InstanceMetrics> getLoneInstanceMetrics() {
    if (allMetric.size() > 1) {
      throw new IllegalArgumentException("More than 1 metrics available, count = " + allMetric.size());
    }

    if (allMetric.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(allMetric.iterator().next().metric);
  }

  /**
   * @return unique names of all metrics present in this {@link ComponentMetrics}
   */
  public Collection<String> getMetricNames() {
    final Collection<String> result = new ArrayList<>();
    result.addAll(metricsDim.keySet());
    return result;
  }

  /**
   * @return unique names of all components present in this {@link ComponentMetrics}
   */
  public Collection<String> getComponentNames() {
    final Collection<String> result = new ArrayList<>();
    result.addAll(componentDim.keySet());
    return result;
  }

  /**
   * @return count of components
   */
  public int getComponentCount() {
    return componentDim.size();
  }

  public MetricsStats computeStats(String metric) {
    double metricMax = 0;
    double metricMin = Double.MAX_VALUE;
    double sum = 0;
    double metricAvg = 0;
    Collection<InstanceMetrics> metrics = filterByMetric(metric).getMetrics();
    for (InstanceMetrics instance : metrics) {
      Double metricValue = instance.getValueSum();
      if (metricValue == null) {
        continue;
      }
      metricMax = metricMax < metricValue ? metricValue : metricMax;
      metricMin = metricMin > metricValue ? metricValue : metricMin;
      sum += metricValue;
    }
    metricAvg = sum / metrics.size();
    return new MetricsStats(metric, metricMin, metricMax, metricAvg);
  }

  /**
   * Merges {@link InstanceMetrics}s in two different {@link ComponentMetrics}. Input objects are not modified. This
   * is a utility method two merge two different metrics. The method will fail if both the input objects contain
   * metrics for the same {@link InstanceMetrics}.
   *
   * @return A new {@link ComponentMetrics} instance
   */
  public static ComponentMetrics merge(ComponentMetrics data1, ComponentMetrics data2) {
    ComponentMetrics mergedData = new ComponentMetrics();
    if (data1 != null) {
      mergedData.addAll(data1.getMetrics());
    }
    if (data2 != null) {
      mergedData.addAll(data2.getMetrics());
    }
    return mergedData;
  }

  private class InstanceMetricWrapper {
    private final InstanceMetrics metric;

    InstanceMetricWrapper(InstanceMetrics metric) {
      this.metric = metric;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      InstanceMetricWrapper that = (InstanceMetricWrapper) o;

      return metric.getComponentName().equals(that.metric.getComponentName())
          && metric.getInstanceName().equals(that.metric.getInstanceName())
          && metric.getMetricName().equals(that.metric.getMetricName());
    }

    @Override
    public int hashCode() {
      int result = metric.getComponentName().hashCode();
      result = 31 * result + metric.getInstanceName().hashCode();
      result = 31 * result + metric.getMetricName().hashCode();
      return result;
    }
  }

  @Override
  public String toString() {
    return "ComponentMetrics{" +
        "allMetric=" + allMetric +
        '}';
  }
}
