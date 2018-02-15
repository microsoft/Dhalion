/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.api;

import com.microsoft.dhalion.core.Measurement;
import com.microsoft.dhalion.policy.PoliciesExecutor.ExecutionContext;

import java.util.Collection;

/**
 * A {@link ISensor} provides {@link Measurement}s for one or more system metrics. For e.g. throughput, latency, etc
 */
public interface ISensor {
  /**
   * @return returns types of metrics whose {@link Measurement}s are fetched by this {@link ISensor}
   */
  default Collection<String> getMetricTypes() {
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
   * Provides {@link Measurement}s of the metrics managed by this {@link ISensor} for all components of the application.
   * The {@link ISensor}'s configuration can be used to customize the result. For e.g. duration for which the
   * {@link Measurement}s are needed and external source configuration.
   *
   * @return latest {@link Measurement}s
   */
  default Collection<Measurement> fetch() {
    throw new UnsupportedOperationException();
  }

  /**
   * Releases all acquired resources and prepare for termination of this {@link ISensor}
   */
  default void close() {
  }
}
