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
 * {@link ComponentSymptom} represents a issue with a component in a distributed system. The issue
 * could be result of multiple {@link InstanceSymptom}s.
 */
public class ComponentSymptom extends Symptom {
  protected ComponentInfo componentInfo;
  protected ComponentMetrics metricsData;

  public ComponentSymptom(ComponentInfo componentInfo, ComponentMetrics metricsData) {
    this.componentInfo = componentInfo;
    this.metricsData = metricsData;
  }

  @Override
  public String toString() {
    return "ComponentSymptom{" +
        "componentInfo=" + componentInfo +
        ", metricsData=" + metricsData +
        '}';
  }

  public ComponentSymptom(ComponentInfo componentInfo) {
    this.componentInfo = componentInfo;
  }

  public ComponentMetrics getMetricsData() {
    return metricsData;
  }

  public static ComponentSymptom from(ComponentMetrics data) {
    return new ComponentSymptom(new ComponentInfo(data.getName()), data);
  }
}

