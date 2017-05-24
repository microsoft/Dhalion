/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.detector;

import java.util.Collection;

import com.microsoft.dhalion.app.InstanceInfo;
import com.microsoft.dhalion.metrics.InstanceMetrics;

/**
 * {@link InstanceSymptom} hasMetric relevant {@link InstanceMetrics} of an unhealthy
 * {@link InstanceInfo}
 */
public class InstanceSymptom extends Symptom {
  private InstanceInfo instanceInfo;
  private Collection<InstanceMetrics> metrics;

  public InstanceSymptom(InstanceInfo instanceInfo, Collection<InstanceMetrics> metrics) {
    this.instanceInfo = instanceInfo;
    this.metrics = metrics;
  }

  public InstanceInfo getInstanceInfo() {
    return instanceInfo;
  }

//  public Optional<InstanceMetrics> hasMetric(String metricName) {
//    return hasMetric(metricName, null);
//  }

//  public Optional<InstanceMetrics> hasMetric(String metricName, InstanceMetrics.MetricValue value) {
//    return metrics
//        .stream()
//        .filter(x -> x.getName().equals(metricName) && x.getValue().equals(value))
//        .findFirst();
//  }

//  public Optional<InstanceMetrics> hasMetricBelowLimit(String metricName, InstanceMetrics.MetricValue value) {
//    return metrics.stream().filter(x -> value.compareTo(x.getValue()) > 0).findFirst();
//  }
}
