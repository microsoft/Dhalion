/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.api;

import java.util.List;
import java.util.Set;

import com.microsoft.dhalion.detector.Symptom;
import com.microsoft.dhalion.state.MetricsSnapshot;

public interface IDetector {
  /**
   * Initializes this instance and should be invoked once by the system before its use.
   */
  default void initialize() {
  }

  /**
   * Detects a problem or anomaly with the distributed application
   *
   * @return a list of issues detected by the symptom detectors
   */
  default Set<Symptom> detect(MetricsSnapshot snapshot) {
    return null;
  }

  /**
   * Release all acquired resources and prepare for termination of this instance
   */
  default void close() {
  }
}
