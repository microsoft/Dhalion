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
import java.util.Collections;

/**
 * A {@link Symptom} identifies an anomaly or a potential health issue in a specific component of a
 * distributed application. For e.g. identification of irregular processing latency.
 */
public class Symptom {
  // symptom identifier
  private final String type;

  // instant when this symptom was created
  private final Instant instant;

  // measurements corresponding to this symptom
  private final Collection<Measurement> measurements;

  public Symptom(String symptomType, Instant instant, Collection<Measurement> measurements) {
    this.type = symptomType;
    this.instant = instant;
    this.measurements = new ArrayList<>(measurements);
  }

  public String getType() {
    return type;
  }

  public Instant getInstant() {
    return instant;
  }

  public Collection<Measurement> getMeasurements() {
    return Collections.unmodifiableCollection(measurements);
  }

  @Override
  public String toString() {
    return "Symptom{" +
        "type='" + type + '\'' +
        ", instant=" + instant +
        ", measurements count=" + measurements.size() +
        '}';
  }
}

