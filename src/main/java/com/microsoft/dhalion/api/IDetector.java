/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.api;

import java.util.List;

import com.microsoft.dhalion.symptom.Symptom;

public interface IDetector extends AutoCloseable {
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
  default List<? extends Symptom> detect() {
    return null;
  }

  /**
   * Release all acquired resources and prepare for termination of this instance
   */
  default void close() {
  }
}
