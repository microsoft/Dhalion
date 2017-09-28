/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.metrics;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Optional;

public class StatsCollector {

  public Map<String, Map<String, Double>> metricStats = new HashMap<>();

  public void updateMetricData(String metricName, String componentName, double currentMaxValue) {
    synchronized (this.metricStats) {
      double value;
      Map<String, Double> componentStats = metricStats.get(metricName);
      if (componentStats != null) {
        value = (componentStats.containsKey(componentName) == true) ? Math.max
            (componentStats.get(componentName), currentMaxValue) : currentMaxValue;
      } else {
        value = currentMaxValue;
      }
      componentStats.put(componentName, value);
      metricStats.put(metricName, componentStats);
    }
  }

  public Optional<Double> getMetricData(String metricName, String componentName) {
    synchronized (this.metricStats) {
      Map<String, Double> componentStats = metricStats.get(metricName);
      if(componentStats != null) {
        if (metricStats.containsKey(componentName)) {
          return Optional.of(componentStats.get(componentName));
        }
      }
      return Optional.absent();
    }
  }
}
