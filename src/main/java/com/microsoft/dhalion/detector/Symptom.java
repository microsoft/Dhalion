/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.detector;

import java.util.HashMap;
import java.util.Map;

import com.microsoft.dhalion.metrics.ComponentMetrics;
import com.microsoft.dhalion.metrics.MetricsStats;

/**
 * A {@link Symptom} identifies an anomaly or a potential health issue in a specific component of a
 * distributed application. For e.g. identification of irregular processing latency.
 */
public class Symptom {
  private String name;
  private Map<String, ComponentMetrics> metrics = new HashMap<>();
  private MetricsStats stats;

  public Symptom(String symptomName) {
    this.name = symptomName;
  }

  public Symptom(String symptomName, ComponentMetrics metrics) {
    this.name = symptomName;
    addComponentMetrics(metrics);
  }

  public Symptom(String symptomName, ComponentMetrics metrics, MetricsStats stats) {
    this.name = symptomName;
    this.stats = stats;
    addComponentMetrics(metrics);
  }

  public synchronized void addComponentMetrics(ComponentMetrics metrics) {
    this.metrics.put(metrics.getName(), metrics);
  }

  public String getName() {
    return name;
  }

  public Map<String, ComponentMetrics> getComponents() {
    return metrics;
  }

  public MetricsStats getStats() {
    return stats;
  }

  /**
   * @return the only component exhibiting this symptom
   */
  public synchronized ComponentMetrics getComponent() {
    if (metrics.size() > 1) {
      throw new IllegalStateException();
    }

    return metrics.values().iterator().next();
  }

  @Override
  public String toString() {
    return "Symptom{" +
        "name=" + name +
        ", metrics=" + metrics +
        '}';
  }
}

