/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.core;

import java.time.Instant;
import java.util.Collection;

/**
 * A {@link Symptom} identifies an anomaly or a potential health issue in a specific component of a
 * distributed application. For e.g. identification of irregular processing latency.
 */
public class Symptom extends Outcome {
  public Symptom(String symptomType, Instant instant, Collection<String> assignments) {
    super(symptomType, instant, assignments);
  }

  public Symptom(int id, String symptomType, Instant instant, Collection<String> assignments) {
    super(id, symptomType, instant, assignments);
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

