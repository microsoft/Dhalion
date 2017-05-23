/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.detector;

import java.util.Collection;

import com.microsoft.dhalion.app.InstanceInfo;
import com.microsoft.dhalion.metrics.InstanceMetricsData;

/**
 * {@link InstanceSymptom} hasMetric relevant {@link InstanceMetricsData} of an unhealthy
 * {@link InstanceInfo}
 */
public class InstanceSymptom extends Symptom {
  private InstanceInfo instanceInfo;
  private Collection<InstanceMetricsData> metrics;

  public InstanceSymptom(InstanceInfo instanceInfo, Collection<InstanceMetricsData> metrics) {
    this.instanceInfo = instanceInfo;
    this.metrics = metrics;
  }

  public InstanceInfo getInstanceInfo() {
    return instanceInfo;
  }

//  public Optional<InstanceMetricsData> hasMetric(String metricName) {
//    return hasMetric(metricName, null);
//  }

//  public Optional<InstanceMetricsData> hasMetric(String metricName, InstanceMetricsData.MetricValue value) {
//    return metrics
//        .stream()
//        .filter(x -> x.getName().equals(metricName) && x.getValue().equals(value))
//        .findFirst();
//  }

//  public Optional<InstanceMetricsData> hasMetricBelowLimit(String metricName, InstanceMetricsData.MetricValue value) {
//    return metrics.stream().filter(x -> value.compareTo(x.getValue()) > 0).findFirst();
//  }
}
