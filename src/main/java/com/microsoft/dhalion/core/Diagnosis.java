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
 * A {@link Diagnosis} is a representation of a possible causes of one or more {@link Symptom}s. For e.g. resource
 * under-provisioning
 */
public class Diagnosis extends Outcome {
  // symtoms referred to create this instance
  private final Collection<Symptom> symptoms = new ArrayList<>();

  public Diagnosis(String type, Instant instant, Collection<String> assignments) {
    this(type, instant, assignments, null);
  }

  public Diagnosis(String type,
                   Instant instant,
                   Collection<String> assignments,
                   Collection<Symptom> symptoms) {
    super(type, instant, assignments);
    if (symptoms != null) {
      this.symptoms.addAll(symptoms);
    }
  }

  Diagnosis(int id, String type, Instant instant, Collection<String> assignments, Collection<Symptom> symptoms) {
    super(id, type, instant, assignments);
    if (symptoms != null) {
      this.symptoms.addAll(symptoms);
    }
  }

  /**
   * @return {@link Symptom}s referred to when this {@link Diagnosis} was created
   */
  public Collection<Symptom> symptoms() {
    return Collections.unmodifiableCollection(symptoms);
  }

  @Override
  public String toString() {
    return "Diagnosis{" +
        "id=" + id() +
        ", type=" + type() +
        ", instant=" + instant() +
        ", assignments=" + assignments() +
        '}';
  }
}
