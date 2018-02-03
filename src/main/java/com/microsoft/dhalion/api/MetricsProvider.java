/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.api;

import com.microsoft.dhalion.core.Measurement;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

/**
 * A {@link MetricsProvider} implements common utility methods to produce {@link Measurement}s. In some cases it will
 * provide for connecting and authorizing with a remote metrics source. Or provide end points where external services
 * can produce {@link Measurement}s. Typically {@link ISensor} will use this implementation to obtain raw metrics.
 * {@link ISensor} may then process and cleanse the metrics.
 */
public interface MetricsProvider {
  /**
   * Initializes this instance and will be invoked once before this instance is used.
   */
  default void initialize() {
  }

  /**
   * Returns raw {@link Measurement}s for all instances of one or more components of a distributed app in a specified
   * time window. For e.g. returns total number of records processed between time t1 and t2  by all instances of a
   * storm bolt.
   *
   * @param startTime  metric aggregation window start time, endTime = startTime - duration
   * @param duration   the duration for which the metric was aggregated
   * @param metrics    ids of the metric
   * @param components ids of the components for which the metric is needed
   * @return collection of {@link Measurement}s
   */
  default Collection<Measurement> getMeasurements(Instant startTime,
                                                  Duration duration,
                                                  Collection<String> metrics,
                                                  Collection<String> components) {
    throw new UnsupportedOperationException("This method is not implemented in the metrics provider");
  }

  /**
   * Release all acquired resources and prepare for termination of this instance
   */
  default void close() {
  }
}
