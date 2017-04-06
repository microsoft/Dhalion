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

import com.microsoft.dhalion.app.ComponentInfo;
import com.microsoft.dhalion.metrics.ComponentMetricsData;

/**
 * {@link ComponentSymptom} represents a issue with a component in a distributed system. The issue
 * could be result of multiple {@link InstanceSymptom}s.
 */
public class ComponentSymptom extends Symptom {
  protected ComponentInfo componentInfo;
  protected ComponentMetricsData metricsData;

  public ComponentSymptom(ComponentInfo componentInfo, ComponentMetricsData metricsData) {
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

  public ComponentMetricsData getMetricsData() {
    return metricsData;
  }

  public static ComponentSymptom from(ComponentMetricsData data) {
    return new ComponentSymptom(new ComponentInfo(data.getName()), data);
  }
}

