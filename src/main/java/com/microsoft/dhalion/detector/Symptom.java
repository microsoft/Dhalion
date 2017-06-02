/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.detector;

import com.microsoft.dhalion.metrics.ComponentMetrics;

/**
 * A {@link Symptom} identifies an anomaly or a potential health issue in a specific component of a
 * distributed application. For e.g. identification of irregular processing latency.
 */
public class Symptom {
  protected String name;
  protected ComponentMetrics metrics;

  public Symptom(String symptomName, ComponentMetrics metrics) {
    this.name = name;
    this.metrics = metrics;
  }

  public String getName() {
    return name;
  }

  public ComponentMetrics getMetrics() {
    return metrics;
  }

  @Override
  public String toString() {
    return "Symptom{" +
        "name=" + name +
        ", metrics=" + metrics +
        '}';
  }
}

