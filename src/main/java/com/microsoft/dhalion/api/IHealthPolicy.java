/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.api;

import com.microsoft.dhalion.detector.Symptom;
import com.microsoft.dhalion.diagnoser.Diagnosis;
import com.microsoft.dhalion.resolver.Action;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A {@link IHealthPolicy} strives to keep a distributed application healthy. It uses one or more of
 * {@link IDetector}s, {@link IDiagnoser}s and {@link IResolver}s to achieve this. It is expected that the policy
 * will be executed periodically.
 */
public interface IHealthPolicy {
  /**
   * Initializes this instance and should be invoked once by the system before its use.
   */
  void initialize(List<ISensor> sensors, List<IDetector> detectors, List<IDiagnoser> diagnosers,
                  List<IResolver> resolvers);

  /**
   * Invoked periodically, this method executes one or more {@link IDetector}s.
   */
  List<Symptom> executeDetectors();

  /**
   * Typically invoked after {@link IDetector}s, this method executes one or more
   * {@link IDiagnoser}s.
   */
  List<Diagnosis> executeDiagnosers(List<Symptom> symptoms);

  /**
   * Selects the most suitable {@link IResolver} based on the set of {@link Diagnosis} objects.
   */
  IResolver selectResolver(List<Diagnosis> diagnosis);

  /**
   * Typically invoked after {@link IDiagnoser}s, this method executes one or more {@link IResolver}
   * to fix any identified issues.
   */
  List<Action> executeResolver(IResolver resolver, List<Diagnosis> diagnosis);

  /**
   * @param unit the desired unit of time
   * @return Returns the remaining delay before re-execution of this policy, in the
   * given time unit.
   */
  long getDelay(TimeUnit unit);

  /**
   * Release all acquired resources and prepare for termination of this instance
   */
  default void close() {
  }
}