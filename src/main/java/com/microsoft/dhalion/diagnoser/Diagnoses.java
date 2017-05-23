/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.diagnoser;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.dhalion.detector.Symptom;

/**
 * A {@link Diagnoses} instance is a representation of a possible causes of one or more
 * {@link Symptom}s. A {@link Symptom} could result in creation of one or more {@link Diagnoses}.
 * Similarly, correlated {@link Symptom}s can result in generation of a {@link Diagnoses} instance.
 */
public class Diagnoses {
  private String id;
  private List<? extends Symptom> symptoms;

  public Diagnoses() {
    symptoms = new ArrayList<>();
  }

  public Diagnoses(List<? extends Symptom> correlatedSymptoms) {
    this.symptoms = correlatedSymptoms;
  }

  public List<? extends Symptom> getSymptoms() {
    return symptoms;
  }

  @Override
  public String toString() {
    return "Diagnoses{" +
        "symptom=" + symptoms +
        '}';
  }
}
