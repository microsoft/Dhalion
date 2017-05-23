/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.api;

import java.util.Map;

import com.microsoft.dhalion.metrics.ComponentMetricsData;

/**
 * A {@link MetricsProvider} implementation will fetch and provide metrics to the consumers. For
 * e.g. a {@link IDetector} may use it to get execute latency for a component.
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
