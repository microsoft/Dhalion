/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.detector;

/**
 * A {@link Symptom} identifies an anomaly or a potential health issue in a specific component or
 * in the distributed application in general. For e.g. identification of irregular processing
 * latency.
 */
public abstract class Symptom {
  private String id;
  private long observedAt;
  private long duration;

  public long getObservedAt() {
    return observedAt;
  }

  public long getDuration() {
    return duration;
  }
}
