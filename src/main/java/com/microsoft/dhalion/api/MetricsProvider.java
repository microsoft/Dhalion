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
package com.microsoft.dhalion.api;

import com.microsoft.dhalion.metrics.MetricsInfo;
import com.microsoft.dhalion.symptom.ComponentInfo;
import com.microsoft.dhalion.symptom.InstanceInfo;

/**
 * A {@link MetricsProvider} implementation will fetch and provide metrics to the consumers. For
 * e.g. a {@link ISymptomDetector} may use it to get execute latency for a component.
 */
public interface MetricsProvider extends AutoCloseable {
  <T extends ComponentInfo> MetricsInfo getComponentMetric(String metricName,
                                                           T component,
                                                           long duration);

  <T extends InstanceInfo> MetricsInfo getInstanceMetric(String metricName,
                                                         T instance,
                                                         long duration);

  /**
   * Release all acquired resources and prepare for termination of this instance
   */
  default void close() {
  }
}
