// Copyright 2017 Microsoft. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.microsoft.dhalion.metrics;

import java.util.HashMap;
import java.util.Map;

/**
 * An {@link ComponentMetricsData} holds metrics information for all instances of a component.
 */
public class ComponentMetricsData {
  // id of the component
  protected String name;

  // Time when instance data was collected
  protected long timestampMillis;

  // Duration over which the metrics were collected
  protected int durationSec;

  // a map of metric name and its value
  private HashMap<String, InstanceMetricsData> metrics = new HashMap<>();

  public ComponentMetricsData(String name) {
    this(name, System.currentTimeMillis(), 0, null);
  }

  public ComponentMetricsData(String name,
                              long timestamp,
                              int durationSec,
                              Map<String, InstanceMetricsData> instanceMetricsData) {
    this.name = name;
    this.timestampMillis = timestamp;
    this.durationSec = durationSec;
    if (instanceMetricsData != null) {
      metrics.putAll(instanceMetricsData);
    }
  }

  public void addInstanceMetric(InstanceMetricsData instanceMetricsData) {
    String instanceName = instanceMetricsData.getName();
    if (metrics.containsKey(instanceName)) {
      throw new IllegalArgumentException("Instance metrics exist: " + name);
    }
    metrics.put(instanceName, instanceMetricsData);
  }

  public HashMap<String, InstanceMetricsData> getMetrics() {
    return metrics;
  }

  public InstanceMetricsData getMetrics(String instanceName) {
    return metrics.get(instanceName);
  }

  public Double getMetricValue(String instance, String metric) {
    InstanceMetricsData instanceMetrics = getMetrics(instance);
    if (instanceMetrics == null) {
      return null;
    }

    return instanceMetrics.getMetric(metric);
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

  public boolean anyInstanceAboveLimit(String metricName, double limit) {
    return metrics.values().stream()
        .filter(x -> x.hasMetricAboveLimit(metricName, limit))
        .findAny().isPresent();
  }

  /**
   * Merges instance metrics in two different objects into one. Input objects are not modified. It
   * is assumed that the two input data sets belong to the same component
   *
   * @return A new {@link ComponentMetricsData} instance
   */
  public static ComponentMetricsData merge(ComponentMetricsData data1, ComponentMetricsData data2) {
    ComponentMetricsData mergedData = new ComponentMetricsData(data1.getName());
    for (InstanceMetricsData instance1 : data1.getMetrics().values()) {
      InstanceMetricsData instance2 = data2.getMetrics(instance1.getName());
      if (instance2 != null) {
        instance1 = InstanceMetricsData.merge(instance1, instance2);
      }
      mergedData.addInstanceMetric(instance1);
    }

    return mergedData;
  }
}
