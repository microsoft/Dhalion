/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.api;

import java.util.List;

import com.microsoft.dhalion.resolver.Action;
import com.microsoft.dhalion.resolver.Proposal;
import com.microsoft.dhalion.symptom.Diagnosis;
import com.microsoft.dhalion.symptom.Symptom;

/**
 * A {@link IHealthPolicy} strives to keep a distributed application healthy. It uses one or more of
 * of {@link ISymptomDetector}s, {@link IDiagnoser}s and {@link IResolver}s to achieve this. It is
 * expected that the policy will be executed periodically.
 */
public interface IHealthPolicy extends AutoCloseable {
  /**
   * Initializes this instance and should be invoked once by the system before its use.
   */
  void initialize();

  /**
   * Invoked periodically, this method executes one or more {@link ISymptomDetector}s.
   */
  List<? extends Symptom> executeDetectors();

  /**
   * Typically invoked after {@link ISymptomDetector}s, this method executes one or more
   * {@link IDiagnoser}s.
   */
  List<Diagnosis<? extends Symptom>> executeDiagnosers(List<? extends Symptom> symptoms);

  /**
   * Returns a list of {@link Proposal}s based on the set of {@link Diagnosis} objects.
   */
  List<Proposal> selectProposals(List<Diagnosis<? extends Symptom>> diagnosis);

  /**
   * Typically invoked after {@link IDiagnoser}s, this method executes one or more {@link IResolver}
   * to fix any identified issues.
   */
  List<Action> executeResolvers(List<Proposal> proposals);

  /**
   * Release all acquired resources and prepare for termination of this instance
   */
  void close();

  /**
   * All policies need to be notified about an update event. System will invoke this method on all
   * policies in case a policy's {@link IResolver} updates the distribution application.
   *
   * @param action the action taken by a resolver
   */
  void onUpdate(Action action);
}
