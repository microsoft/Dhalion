/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.diagnoser;

import java.util.Set;

import com.microsoft.dhalion.common.InstanceInfo;
import com.microsoft.dhalion.detector.Symptom;

/**
 * A {@link Diagnosis} instance is a representation of a possible causes of one or more
 * {@link Symptom}s.
 */
public class Diagnosis {
  private String diagnosisType;
  private String diagnosisName;
  private Set<String> symptomNames;
  private Set<InstanceInfo> addresses;

  public Diagnosis(String diagnosisType, String diagnosisName, Set<String> symptomNames,
                   Set<InstanceInfo> addresses) {
    this.diagnosisType = diagnosisType;
    this.diagnosisName = diagnosisName;
    this.symptomNames = symptomNames;
    this.addresses = addresses;
  }

  public String getDiagnosisType() {
    return diagnosisType;
  }

  public String getDiagnosisName() {
    return diagnosisName;
  }

  public Set<String> getSymptomNames() {
    return symptomNames;
  }

  public Set<InstanceInfo> getAddresses() {
    return addresses;
  }

  @Override
  public String toString() {
    return "Diagnosis{" +
        "diagnosisType='" + diagnosisType + '\'' +
        ", diagnosisName='" + diagnosisName + '\'' +
        ", symptomNames=" + symptomNames +
        ", addresses=" + addresses +
        '}';
  }
}
