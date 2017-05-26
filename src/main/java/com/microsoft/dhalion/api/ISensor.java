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
 * A {@link ISensor} typically provides a system metric. For e.g. execute count
 */
public interface ISensor extends AutoCloseable {
  /**
   * @return returns a map of component id to metric value for all components
   */
  default Map<String, ComponentMetrics> get(){
    return null;
  }

  /**
   * @return returns a map of component id to metric value for specific components
   */
  default Map<String, ComponentMetrics> get(String... components) {
    return null;
  }
}
