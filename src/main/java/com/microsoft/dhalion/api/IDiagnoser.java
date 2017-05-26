/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.api;

import java.util.List;
import java.util.Map;

import com.microsoft.dhalion.detector.Symptom;
import com.microsoft.dhalion.diagnoser.Diagnosis;

/**
 * A {@link IDiagnoser} evaluates one or more {@link Symptom}s and produces a {@link Diagnosis}, if
 * any, representing a possible problem responsible for the observed {@link Symptom}s.
 */
public interface IDiagnoser extends AutoCloseable {
  /**
   * Initializes this instance and should be invoked once by the system before its use.
   */
  default void initialize() {
  }

  /**
   * Evaluates available {@link Symptom}s and determines if a problem exists
   *
   * @return a {@link Diagnosis} instance representing a problem
   */
  default Diagnosis diagnose(List<Symptom> symptoms) {
    return null;
  }

  /**
   * Release all acquired resources and prepare for termination of this instance
   */
  default void close() {
  }
}
