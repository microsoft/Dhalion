// Copyright 2017 Microsoft. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
