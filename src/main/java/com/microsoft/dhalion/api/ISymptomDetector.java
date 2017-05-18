/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.api;

import java.util.Collection;

import com.microsoft.dhalion.symptom.Symptom;

public interface ISymptomDetector<T extends Symptom> extends AutoCloseable {
  /**
   * Initializes this instance and should be invoked once by the system before its use.
   */
  void initialize();

  /**
   * Detects a problem or issue with the distributed application
   * @return the collection of issues detected by the symptom detectors
   */
  Collection<T> detect();

  /**
   * Release all acquired resources and prepare for termination of this instance
   */
  default void close() {
  }
}
