/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.microsoft.dhalion.api.ISensor;

public class MetricsCollector implements Runnable {
  private static final Logger LOG = Logger.getLogger(MetricsCollector.class.getName());
  private StatsCollector statsCollector;
  private List<ISensor> sensors = new ArrayList<>();


  public MetricsCollector(StatsCollector statsCollector, List<ISensor> sensors) {
    this.statsCollector = statsCollector;
    this.sensors = sensors;
  }

  public void run() {
    for (int i = 0; i < sensors.size(); i++) {
      Map<String, ComponentMetrics> metrics = sensors.get(i).get();
      for (Map.Entry<String, ComponentMetrics> entry : metrics.entrySet()) {
        String componentName = entry.getKey();
        ComponentMetrics componentMetrics = entry.getValue();
        for (Map.Entry<String, InstanceMetrics> instanceMetricsEntry : componentMetrics.getMetrics
            ().entrySet()) {
          String metricName = instanceMetricsEntry.getKey();
          double maxValue = componentMetrics.computeMinMaxStats(metricName).getMetricMax();
          statsCollector.updateMetricData(metricName, componentName, maxValue);
        }
      }
    }
  }

}
