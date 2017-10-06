/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.api;

import java.util.Map;

import com.microsoft.dhalion.metrics.ComponentMetrics;
import com.microsoft.dhalion.metrics.MetricsStats;
import com.microsoft.dhalion.state.State;

/**
 * A {@link ISensor} typically provides a system metric. For e.g. execute count
 */
public interface ISensor {


  /**
   * Initializes sensor
   */
  default void initialize(State stateSnapshot) {
  }

  /**
   * Collects the appropriate metrics and updates the state snapshot
   */
  default Map<String, ComponentMetrics> fetchMetrics() {
    return null;
  }

  /**
   * Collects the appropriate stats and updates the state snapshot
   */
  default Map<String, MetricsStats> fetchStats() {
    return null;
  }

  /**
   * @return returns a map of component id to metric value for all components
   */
  default Map<String, ComponentMetrics> get() {
    return null;
  }

  /**
   * @return returns a map of component id to metric value for specific components
   */
  default Map<String, ComponentMetrics> get(String... components) {
    return null;
  }

  /**
   * Release all acquired resources and prepare for termination of this instance
   */
  default void close() {
  }

  default String getMetricName() {
    return null;
  }

  default MetricsStats getStats(String component) {
    return null;
  }

}
