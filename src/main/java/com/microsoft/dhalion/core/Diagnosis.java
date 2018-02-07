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
public class Diagnosis {
  // diagnosis identifier
  private final String type;

  // instant when this diagnosis was created
  private final Instant instant;

  // symptoms corresponding to this symptom
  private final Collection<Symptom> symptoms;

  public Diagnosis(String type, Instant instant, Collection<Symptom> symptoms) {
    this.type = type;
    this.instant = instant;
    this.symptoms = new ArrayList<>(symptoms);
  }

  public String getType() {
    return type;
  }

  public Instant getInstant() {
    return instant;
  }

  public Collection<Symptom> getSymptoms() {
    return Collections.unmodifiableCollection(symptoms);
  }

  @Override
  public String toString() {
    return "Diagnosis{" +
        "type='" + type + '\'' +
        ", instant=" + instant +
        ", symptoms=" + symptoms +
        '}';
  }
}
