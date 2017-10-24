/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.metrics;

import com.microsoft.dhalion.common.DuplicateMetricException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link ComponentMetrics} is a collection of {@link InstanceMetric} objects organized in a 2D space. This class holds
 * metric information for all instances of all components. The dimensions are metric name and component name. This
 * class provides methods to filter {@link InstanceMetric} objects by along either of the two dimensions.
 */
public class ComponentMetrics {
  //Map for the component name dimension
  private HashMap<String, Set<InstanceMetricWrapper>> componentDim = new HashMap<>();

  //Map for the metric name dimension
  private HashMap<String, Set<InstanceMetricWrapper>> metricsDim = new HashMap<>();

  //Set of all metrics managed by this object
  private Set<InstanceMetricWrapper> allMetric = new HashSet<>();

  public synchronized void addAll(Collection<InstanceMetric> metrics) {
    metrics.forEach(this::add);
  }

  public synchronized void add(InstanceMetric metric) {
    if (allMetric.contains(metric)) {
      throw new DuplicateMetricException(metric.getComponentName(), metric.getInstanceName(), metric.getMetricName());
    }

    InstanceMetricWrapper wrappedMetric = new InstanceMetricWrapper(metric);
    allMetric.add(wrappedMetric);
    componentDim.computeIfAbsent(metric.getComponentName(), k -> new HashSet<>()).add(wrappedMetric);
    metricsDim.computeIfAbsent(metric.getMetricName(), k -> new HashSet<>()).add(wrappedMetric);
  }

  public void addMetric(String component, String instance, String metricName, double value) {
    InstanceMetric metric = new InstanceMetric(component, instance, metricName);
    metric.addValue(value);
    add(metric);
  }

  /**
   * @param componentName
   * @return a new {@link ComponentMetrics} instance containing all {@link InstanceMetric}s belonging to {@code
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
   * @param metricName
   * @return a new {@link ComponentMetrics} instance containing all {@link InstanceMetric}s belonging to {@code
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
   * @param componentName
   * @param instanceName
   * @return a new {@link ComponentMetrics} instance containing all {@link InstanceMetric}s belonging to {@code
   * componentName/instanceName} only.
   */
  public ComponentMetrics filterByInstance(String componentName, String instanceName) {
    final ComponentMetrics result = new ComponentMetrics();
    Collection<InstanceMetric> metrics = filterByComponent(componentName).getMetrics();
    if (metrics != null) {
      metrics.stream()
          .filter(metric -> metric.getInstanceName().equals(instanceName))
          .forEach(result::add);
    }

    return result;
  }

  /**
   * @return all {@link InstanceMetric} managed by this {@link ComponentMetrics} object
   */
  public Collection<InstanceMetric> getMetrics() {
    final Collection<InstanceMetric> result = new ArrayList<>();
    allMetric.forEach(wrapper -> result.add(wrapper.metric));
    return result;
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
   * Merges {@link InstanceMetric}s in two different {@link ComponentMetrics}. Input objects are not modified. This
   * is a utility method two merge two different metrics. The method will fail if both the input objects contain
   * metrics for the same {@link InstanceMetric}.
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
    private final InstanceMetric metric;

    public InstanceMetricWrapper(InstanceMetric metric) {
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
