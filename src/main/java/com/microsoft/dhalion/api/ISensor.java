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
 * A {@link ISensor} typically provides a system metric. For e.g. execute count
 */
public interface ISensor extends AutoCloseable {
  /**
   * @return returns a map of component id to metric value for all components
   */
  Map<String, ComponentMetricsData> get();

  /**
   * @return returns a map of component id to metric value for specific components
   */
  Map<String, ComponentMetricsData> get(String... components);

  /**
   * Release all acquired resources and prepare for termination of this instance
   */
  default void close() {
  }
}
