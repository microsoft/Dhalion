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
package com.microsoft.dhalion.symptom;

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
