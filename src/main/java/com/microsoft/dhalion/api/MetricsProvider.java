/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.api;

import java.util.Map;

import com.microsoft.dhalion.metrics.ComponentMetrics;

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
  default Map<String, ComponentMetrics> getComponentMetrics(String metric,
                                                            int durationSec,
                                                            String... component) {
    return null;
  }

  /**
   * Returns metric value for all instances of one or more components of a distributed app in a
   * specified time window. For e.g. returns total number of records processed between time t1 and
   * t2 by all instances of a storm bolt. The implementation may return multiple records per
   * instance. For e.g. the implementation may return 3 records, one per minute, for an instance for
   * a 3 minute long time window.
   *
   * @param metric id of the metric
   * @param startTimeSec metric aggregation window start time, endTime = startTimeSec - durationSec
   * @param durationSec the duration for which the metric was aggregated
   * @param component ids of the components for which the metric is needed
   * @return the map of component id to component metrics
   */
  default Map<String, ComponentMetrics> getComponentMetrics(String metric,
                                                            int startTimeSec,
                                                            int durationSec,
                                                            String... component) {
    return null;
  }

  /**
   * Release all acquired resources and prepare for termination of this instance
   */
  default void close() {
  }
}
