/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.detector;

import java.util.Set;

import com.microsoft.dhalion.common.InstanceInfo;

/**
 * A {@link Symptom} identifies an anomaly or a potential health issue in a specific component of a
 * distributed application. For e.g. identification of irregular processing latency.
 */
public class Symptom {
  private String symptomName;
  private String symptomType;
  private Set<String> addresses;

  public Symptom(String symptomName, String symptomType, Set<String> addresses) {
    this.symptomName = symptomName;
    this.symptomType = symptomType;
    this.addresses = addresses;
  }

  public String getSymptomName() {
    return symptomName;
  }

  public String getSymptomType() {
    return symptomType;
  }

  public Set<String> getAddresses() {
    return addresses;
  }

  public boolean refersTo(String address){
    return addresses.contains(address);
  }

  @Override
  public String toString() {
    return "Symptom{" +
        "symptomName='" + symptomName + '\'' +
        ", symptomType='" + symptomType + '\'' +
        ", addresses=" + addresses +
        '}';
  }
}