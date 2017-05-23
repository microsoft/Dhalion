/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.api;

import java.util.List;

import com.microsoft.dhalion.diagnoser.Diagnoses;
import com.microsoft.dhalion.resolver.Action;
import com.microsoft.dhalion.detector.Symptom;

/**
 * A {@link IHealthPolicy} strives to keep a distributed application healthy. It uses one or more of
 * of {@link IDetector}s, {@link IDiagnoser}s and {@link IResolver}s to achieve this. It is
 * expected that the policy will be executed periodically.
 */
public interface IHealthPolicy extends AutoCloseable {
  /**
   * Initializes this instance and should be invoked once by the system before its use.
   */
  default void initialize() {
  }

  /**
   * Invoked periodically, this method executes one or more {@link IDetector}s.
   */
  default List<? extends Symptom> executeDetectors() {
    return null;
  }

  /**
   * Typically invoked after {@link IDetector}s, this method executes one or more
   * {@link IDiagnoser}s.
   */
  default List<Diagnoses> executeDiagnosers(List<? extends Symptom> symptoms) {
    return null;
  }

  /**
   * Selects the most suitable {@link IResolver} based on the set of {@link Diagnoses} objects.
   */
  default IResolver selectResolver(List<Diagnoses> diagnosis) {
    return null;
  }

  /**
   * Typically invoked after {@link IDiagnoser}s, this method executes one or more {@link IResolver}
   * to fix any identified issues.
   */
  default List<Action> executeResolvers(IResolver resolver) {
    return null;
  }

  /**
   * Release all acquired resources and prepare for termination of this instance
   */
  default void close() {
  }

  /**
   * All policies need to be notified about an update event. System will invoke this method on all
   * policies in case a policy's {@link IResolver} updates the distribution application.
   *
   * @param action the action taken by a resolver
   */
  default void onUpdate(Action action) {
  }
}
