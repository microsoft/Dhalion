/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.api;

import com.microsoft.dhalion.metrics.ComponentMetrics;
import com.microsoft.dhalion.metrics.InstanceMetrics;

import java.time.Duration;
import java.time.Instant;

/**
 * A {@link MetricsProvider} implementation will fetch and provide metrics to the consumers. For
 * e.g. a {@link IDetector} may use it to get execute latency for a component.
 */
public interface MetricsProvider {
  /**
   * Returns metric value for all instances of one or more {@code components}. For e.g.
   * returns total number of records processed in 60 seconds by all instances of a storm bolt.
   *
   * @param metric     id of the metric
   * @param duration   the duration for which the metric was aggregated
   * @param components ids of the components for which the metric is needed
   * @return components metrics
   */
  default ComponentMetrics getComponentMetrics(String metric, Duration duration, String... components) {
    return null;
  }

  /**
   * Returns metric value for a specific {@code instance} of a {@code component}. For e.g.
   * returns total number of records processed in 60 seconds by instance-1 of a storm bolt.
   *
   * @param metric    id of the metric
   * @param duration  the duration for which the metric was aggregated
   * @param component id of the components for which the metric is needed
   * @param instance  id of the instance
   * @return InstanceMetrics containing the value(s)
   */
  default InstanceMetrics getInstanceMetrics(String metric, Duration duration, String component, String instance) {
    return null;
  }

  /**
   * Returns metric value for all instances of one or more components in a specified time window.
   * For e.g. returns total number of records processed between time t1 and t2 by all instances
   * of a storm bolt. The implementation may return multiple records per instance. For e.g. the
   * implementation may return 3 records, one per minute, for an instance for a 3 minute long time
   * window.
   *
   * @param metric     id of the metric
   * @param startTime  metric aggregation window start time, endTime = startTime - duration
   * @param duration   the duration for which the metric was aggregated
   * @param components ids of the components for which the metric is needed
   * @return components metrics
   */
  default ComponentMetrics getComponentMetrics(String metric,
                                               Instant startTime,
                                               Duration duration,
                                               String... components) {
    return null;
  }

  /**
   * Returns metric value for all instances of a specific instance of a components in a specified time
   * window. For e.g. returns total number of records processed between time t1 and t2 by all instances
   * of a storm bolt. The implementation may return multiple records per instance. For e.g. the
   * implementation may return 3 records, one per minute, for an instance for a 3 minute long time
   * window.
   *
   * @param metric    id of the metric
   * @param startTime metric aggregation window start time, endTime = startTime - duration
   * @param duration  the duration for which the metric was aggregated
   * @param component ids of the components for which the metric is needed
   * @param instance  id of the instance
   * @return component metrics
   */
  default InstanceMetrics getInstanceMetrics(String metric,
                                             Instant startTime,
                                             Duration duration,
                                             String component,
                                             String instance) {
    return null;
  }

  /**
   * Release all acquired resources and prepare for termination of this instance
   */
  default void close() {
  }
}
