/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A {@link Symptom} identifies an anomaly or a potential health issue in a specific component of a
 * distributed application. For e.g. identification of irregular processing latency.
 */
public class Symptom extends Outcome {
  // measurements referred to create this instance
  private final Collection<Measurement> measurements = new ArrayList<>();

  public Symptom(String type, Instant instant, Collection<String> assignments) {
    this(type, instant, assignments, null);
  }

  public Symptom(String symptomType,
                 Instant instant,
                 Collection<String> assignments,
                 Collection<Measurement> measurements) {
    super(symptomType, instant, assignments);
    if (measurements != null) {
      this.measurements.addAll(measurements);
    }
  }

  Symptom(int id,
          String symptomType,
          Instant instant,
          Collection<String> assignments,
          Collection<Measurement> measurements) {
    super(id, symptomType, instant, assignments);
    if (measurements != null) {
      this.measurements.addAll(measurements);
    }
  }

  /**
   * @return {@link Measurement}s referred to when this {@link Symptom} was created
   */
  public Collection<Measurement> measurements() {
    return measurements;
  }

  @Override
  public String toString() {
    return "Symptom{" +
        "type=" + type() +
        ", id=" + id() +
        ", instant=" + instant() +
        ", assignments=" + assignments() +
        '}';
  }
}

