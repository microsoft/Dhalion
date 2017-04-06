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

import java.util.Map;

import com.microsoft.dhalion.metrics.ComponentMetricsData;

/**
 * A {@link MetricsProvider} implementation will fetch and provide metrics to the consumers. For
 * e.g. a {@link ISymptomDetector} may use it to get execute latency for a component.
 */
public interface MetricsProvider extends AutoCloseable {

  /**
   * Returns metric value for all instances of one or more components of a distributed app. For e.g.
   * returns total number of records processed in 60 seconds by all instances of a storm bolt.
   *
   * @param metric id of the metric
   * @param durationSec the duration for which the metric was aggregated
   * @param component ids of the components for which the metric is needed
   * @return the map of component id to component metrics
   */
  Map<String, ComponentMetricsData> getComponentMetrics(String metric,
                                                        int durationSec,
                                                        String... component);

  /**
   * Release all acquired resources and prepare for termination of this instance
   */
  default void close() {
  }
}
