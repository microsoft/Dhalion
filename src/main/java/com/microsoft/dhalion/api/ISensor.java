//  Copyright 2017 Twitter. All rights reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License
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
