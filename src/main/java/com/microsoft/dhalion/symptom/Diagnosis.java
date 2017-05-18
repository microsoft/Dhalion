/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.symptom;

import java.util.HashSet;
import java.util.Set;

/**
 * A {@link Diagnosis} instance is a representation of a possible causes of one or more
 * {@link Symptom}s. A {@link Symptom} could result in creation of one or more {@link Diagnosis}.
 * Similarly, correlated {@link Symptom}s can result in generation of a {@link Diagnosis} instance.
 */
public class Diagnosis<T extends Symptom> {
  private Set<T> symptoms;

  public Diagnosis() {
    symptoms = new HashSet<>();
  }

  public Diagnosis(Set<T> correlatedSymptoms) {
    this.symptoms = correlatedSymptoms;
  }

  public Set<T> getSymptoms() {
    return symptoms;
  }

  public void addSymptom(T item) {
    symptoms.add(item);
  }

  @Override
  public String toString() {
    return "Diagnosis{" +
        "symptom=" + symptoms +
        '}';
  }
}
