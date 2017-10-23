/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.api;

import com.microsoft.dhalion.metrics.ComponentMetrics;
import com.microsoft.dhalion.metrics.MetricsStats;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link ISensor} typically provides a system metric. For e.g. execute count
 */
public interface ISensor {
  /**
   * @return returns name of the metric on which this sensor operates
   */
  default String getMetricName() {
    return null;
  }

  /**
   * Pulls a given metric information for all component from an external source. The sensor instance then manages the
   * pulled information in local state. It also updates the {@link MetricsStats} associated with this sensor.
   */
  default Map<String, ComponentMetrics> fetchMetrics() {
    return null;
  }

  /**
   * @return returns the most recently fetched metric value for all components as a map
   */
  default Map<String, ComponentMetrics> getMetrics() {
    return new HashMap<>();
  }

  /**
   * @return returns the most recently fetched metric value for a specific component
   */
  default Optional<ComponentMetrics> getMetrics(String component) {
    return Optional.empty();
  }

  /**
   * @return returns latest metric stats for all components as a map
   */
  default Map<String, MetricsStats> getStats() {
    return new HashMap<>();
  }

  /**
   * @return returns latest metric stats for a specific component
   */
  default Optional<MetricsStats> getStats(String component) {
    return Optional.empty();
  }

  /**
   * Releases all acquired resources and prepare for termination of this instance
   */
  default void close() {
  }
}
