/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.api;

import com.microsoft.dhalion.core.Measurement;
import com.microsoft.dhalion.core.Symptom;
import com.microsoft.dhalion.policy.PoliciesExecutor.ExecutionContext;

import java.util.Collection;

/**
 * {@link IDetector} typically examines {@link Measurement}s and produce {@link Symptom}s for any observed anomalies or
 * system health issues.
 */
public interface IDetector {
  /**
   * @return returns types of {@link Symptom}s created by this {@link IDetector}
   */
  default Collection<String> getSymptomTypes() {
    throw new UnsupportedOperationException();
  }

  /**
   * Initializes this instance and will be invoked once before this instance is used.
   *
   * @param context execution context for this instance
   */
  default void initialize(ExecutionContext context) {
  }

  /**
   * Triggers system health examination typically using latest {@link Measurement}s and produces {@link Symptom}s
   * representing the observations.
   *
   * @param measurements most recently fetched {@link Measurement}s
   * @return the {@link Symptom}s created using latest observations
   */
  default Collection<Symptom> detect(Collection<Measurement> measurements) {
    throw new UnsupportedOperationException();
  }

  /**
   * Releases all acquired resources and prepare for termination of this {@link IDetector}
   */
  default void close() {
  }
}
