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
 * A {@link Diagnosis} is a representation of a possible causes of one or more {@link Symptom}s. For e.g. resource
 * under-provisioning
 */
public class Diagnosis extends Outcome {
  public Diagnosis(String type, Instant instant, Collection<String> assignments) {
    super(type, instant, assignments);
  }

  public Diagnosis(int id, String symptomType, Instant instant, Collection<String> assignments) {
    super(id, symptomType, instant, assignments);
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
