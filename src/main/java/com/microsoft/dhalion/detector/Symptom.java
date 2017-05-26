/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.detector;

import com.microsoft.dhalion.app.ComponentInfo;
import com.microsoft.dhalion.metrics.ComponentMetrics;

/**
 * A {@link Symptom} identifies an anomaly or a potential health issue in a specific component of a
 * distributed application. For e.g. identification of irregular processing latency.
 */
public class Symptom {
  protected String name;
  protected ComponentInfo componentInfo;
  protected ComponentMetrics metrics;

  public Symptom(ComponentInfo componentInfo, ComponentMetrics metrics) {
    this.componentInfo = componentInfo;
    this.metrics = metrics;
  }

  public String getName() {
    return name;
  }

  public Symptom(ComponentInfo componentInfo) {
    this.componentInfo = componentInfo;
  }

  public ComponentMetrics getMetrics() {
    return metrics;
  }

  public static Symptom from(ComponentMetrics data) {
    return new Symptom(new ComponentInfo(data.getName()), data);
  }

  @Override
  public String toString() {
    return "Symptom{" +
        "componentInfo=" + componentInfo +
        ", metrics=" + metrics +
        '}';
  }
}

