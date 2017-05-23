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

/**
 * A {@link IResolver}'s major goal is to resolve the anomaly identified by a {@link Diagnoses}.
 * Input to a {@link IResolver} is a {@link Diagnoses} instance and based on that, it executes
 * appropriate action to bring a linked component or system back to a healthy state.
 */
public interface IResolver extends AutoCloseable {
  /**
   * This method is invoked once to initialize the {@link IResolver} instance
   */
  default void initialize() {
  }

  /**
   * {@link IResolver#resolve} is invoked to fix one or more problems identified in the
   * {@link Diagnoses} instance.
   *
   * @param diagnosis a list of anomalies detected by a {@link IDiagnoser}s
   * @return all the actions executed by this resolver to mitigate the problems
   */
  default List<Action> resolve(List<Diagnoses> diagnosis){
    return null;
  }

  /**
   * Release all acquired resources and prepare for termination of this instance
   */
  default void close() {
  }
}
