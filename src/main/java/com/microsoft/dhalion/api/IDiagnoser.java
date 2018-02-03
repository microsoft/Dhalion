/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.api;

import com.microsoft.dhalion.core.Symptom;
import com.microsoft.dhalion.core.Diagnosis;

import java.util.Collection;

/**
 * A {@link IDiagnoser} examines and correlates one or more {@link Symptom}s and tries to identify a root cause. If
 * a reason is found, {@link IDetector} produces a {@link Diagnosis} representing a possible problem responsible for
 * the observed {@link Symptom}s.
 */
public interface IDiagnoser {
  /**
   * @return returns names of {@link Diagnosis}s created by this {@link IDiagnoser}
   */
  default Collection<String> getDiagnosisNames() {
    throw new UnsupportedOperationException();
  }

  /**
   * Initializes this instance and will be invoked once before this instance is used.
   */
  default void initialize() {
  }

  /**
   * Triggers examination of available {@link Symptom}s and to identify a health issue responsible for the
   * {@link Symptom}s.
   *
   * @param symptoms recently identified {@link Symptom}s
   * @return a {@link Diagnosis} instance representing a problem
   */
  default Collection<Diagnosis> diagnose(Collection<Symptom> symptoms) {
    throw new UnsupportedOperationException();
  }

  /**
   * Release all acquired resources and prepare for termination of this instance
   */
  default void close() {
  }
}
