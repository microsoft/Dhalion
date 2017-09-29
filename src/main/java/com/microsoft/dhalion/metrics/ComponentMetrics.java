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
 * An {@link ComponentMetrics} holds metrics information for all instances of a component.
 */
public class ComponentMetrics {
  // id of the component
  protected String componentName;

  // a map of instance name to its metric values
  private HashMap<String, InstanceMetrics> metrics = new HashMap<>();

  public ComponentMetrics(String compName) {
    this(compName, null);
  }

  public ComponentMetrics(String compName, String instanceName, String metricName, double value) {
    this(compName, null);
    if (instanceName != null) {
      InstanceMetrics instanceMetrics = new InstanceMetrics(instanceName, metricName, value);
      addInstanceMetric(instanceMetrics);
    }
  }

  public ComponentMetrics(String compName, Map<String, InstanceMetrics> instanceMetricsData) {
    this.componentName = compName;
    if (instanceMetricsData != null) {
      instanceMetricsData.values().stream().forEach(x -> addInstanceMetric(x));
    }
  }

  public void addInstanceMetric(InstanceMetrics instanceMetrics) {
    String instanceName = instanceMetrics.getName();
    if (metrics.containsKey(instanceName)) {
      throw new IllegalArgumentException("Instance metrics exist: " + componentName);
    }
    metrics.put(instanceName, instanceMetrics);
  }

  public HashMap<String, InstanceMetrics> getMetrics() {
    return metrics;
  }

  public InstanceMetrics getMetrics(String instanceName) {
    return metrics.get(instanceName);
  }

  /**
   * @param instance name of the instance for which metrics are desired
   * @param metric   metric name
   * @return all known metric values for the requested instance
   */
  public Map<Instant, Double> getMetricValues(String instance, String metric) {
    InstanceMetrics instanceMetrics = getMetrics(instance);
    if (instanceMetrics == null) {
      return null;
    }

    return instanceMetrics.getMetrics().get(metric);
  }

  /**
   * @param instance name of the instance for which metrics are desired
   * @param metric   metric name
   * @return sum of all the values of the requested metric for the instance.
   */
  public Double getMetricValueSum(String instance, String metric) {
    InstanceMetrics instanceMetrics = getMetrics(instance);
    if (instanceMetrics == null) {
      return null;
    }

    return instanceMetrics.getMetricValueSum(metric);
  }

  public MetricsStats computeStats(String metric) {
    double metricMax = 0;
    double metricMin = Double.MAX_VALUE;
    double sum = 0;
    double metricAvg = 0;
    for (InstanceMetrics instance : this.getMetrics().values()) {

      Double metricValue = instance.getMetricValueSum(metric);
      if (metricValue == null) {
        continue;
      }
      metricMax = metricMax < metricValue ? metricValue : metricMax;
      metricMin = metricMin > metricValue ? metricValue : metricMin;
      sum += metricValue;
    }
    metricAvg = sum / this.getMetrics().size();
    return new MetricsStats(metricMin, metricMax, metricAvg);
  }


  public String getComponentName() {
    return componentName;
  }

  public boolean anyInstanceAboveLimit(String metricName, double limit) {
    return metrics.values().stream().anyMatch(x -> x.hasMetricAboveLimit(metricName, limit));
  }

  /**
   * Merges instance metrics in two different objects into one. Input objects are not modified. It
   * is assumed that the two input data sets belong to the same component. Hence the data sets
   * will contain the same instances.
   *
   * @return A new {@link ComponentMetrics} instance
   */
  public static ComponentMetrics merge(ComponentMetrics data1, ComponentMetrics data2) {
    ComponentMetrics mergedData = new ComponentMetrics(data1.getComponentName());
    for (InstanceMetrics instance1 : data1.getMetrics().values()) {
      InstanceMetrics instance2 = data2.getMetrics(instance1.getName());
      if (instance2 != null) {
        instance1 = InstanceMetrics.merge(instance1, instance2);
      }
      mergedData.addInstanceMetric(instance1);
    }

    return mergedData;
  }

  @Override
  public String toString() {
    return "ComponentMetrics{" +
        "name='" + componentName + '\'' +
        ", metrics=" + metrics +
        '}';
  }
}
