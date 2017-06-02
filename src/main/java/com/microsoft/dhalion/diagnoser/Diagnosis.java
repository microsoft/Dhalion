/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.diagnoser;

import java.util.HashMap;
import java.util.Map;

import com.microsoft.dhalion.detector.Symptom;

/**
 * A {@link Diagnosis} instance is a representation of a possible causes of one or more
 * {@link Symptom}s. A {@link Symptom} could result in creation of one or more {@link Diagnosis}.
 * Similarly, correlated {@link Symptom}s can result in generation of a {@link Diagnosis} instance.
 */
public class Diagnosis {
  private String name;
  private Map<String, Symptom> symptoms;

  public Diagnosis(String diagnosisName) {
    this(diagnosisName, new HashMap<>());
  }

  public Diagnosis(String diagnosisName, Symptom symptom) {
    this(diagnosisName, new HashMap<>());
    symptoms.put(symptom.getName(), symptom);
  }

  public Diagnosis(String diagnosisName, Map<String, Symptom> correlatedSymptoms) {
    this.name = diagnosisName;
    this.symptoms = correlatedSymptoms;
  }

  public String getName() {
    return name;
  }

  public Map<String, Symptom> getSymptoms() {
    return symptoms;
  }

  @Override
  public String toString() {
    return "Diagnosis{" +
        "name='" + name + '\'' +
        ", symptoms=" + symptoms +
        '}';
  }
}
