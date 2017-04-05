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

import java.util.Collection;

import com.microsoft.dhalion.resolver.Action;
import com.microsoft.dhalion.symptom.Diagnosis;
import com.microsoft.dhalion.symptom.Symptom;

/**
 * A {@link IResolver}'s major goal is to resolve the anomaly identified by a {@link Diagnosis}.
 * Input to a {@link IResolver} is a {@link Diagnosis} instance and based on that, it executes
 * appropriate action to bring a linked component or system back to a healthy state.
 */
public interface IResolver<T extends Symptom> extends AutoCloseable {
  /**
   * This method is invoked once to initialize the {@link IResolver} instance
   */
  void initialize();

  /**
   * {@link IResolver#resolve} is invoked to fix one or more problems identified in the
   * {@link Diagnosis} instance.
   *
   * @param diagnosis the anomaly detected by a detector
   * @return all the actions executed by this resolver to mitigate the problems
   */
  Collection<Action> resolve(Diagnosis<T> diagnosis);

  /**
   * Release all acquired resources and prepare for termination of this instance
   */
  default void close() {
  }
}
