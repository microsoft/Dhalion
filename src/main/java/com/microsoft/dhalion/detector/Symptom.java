/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.detector;

import com.microsoft.dhalion.metrics.ComponentMetrics;
import com.microsoft.dhalion.metrics.MetricsStats;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link Symptom} identifies an anomaly or a potential health issue in a specific component of a
 * distributed application. For e.g. identification of irregular processing latency.
 */
public class Symptom {
  private String symptomName;
  private ComponentMetrics metrics = new ComponentMetrics();
  private Map<String, MetricsStats> stats = new HashMap<>();

  public Symptom(String symptomName) {
    this(symptomName, null);
  }

  public Symptom(String symptomName, ComponentMetrics metrics) {
    this(symptomName, metrics, null);
  }

  public Symptom(String symptomName, ComponentMetrics metrics, MetricsStats stats) {
    this.symptomName = symptomName;
    this.metrics = metrics;
    // TODO optimize stats structure like ComponentMetrics
//    addStats(metrics.getComponentName(), stats);
  }

  public synchronized void addComponentMetrics(ComponentMetrics metrics) {
    this.metrics = ComponentMetrics.merge(metrics, this.metrics);
  }

  public synchronized void addStats(String componentName, MetricsStats componentStats) {
    this.stats.put(componentName, componentStats);
  }

  public String getSymptomName() {
    return symptomName;
  }

  public ComponentMetrics getComponentMetrics() {
    return metrics;
  }

  public Map<String, MetricsStats> getStats() {
    return stats;
  }

  @Override
  public String toString() {
    return "Symptom{" +
        "symptomName='" + symptomName + '\'' +
        ", metrics=" + metrics +
        ", stats=" + stats +
        '}';
  }
}