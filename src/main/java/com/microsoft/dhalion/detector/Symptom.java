/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.detector;

import com.microsoft.dhalion.metrics.Measurement;

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
  private final String name;

  // instant when this symptom was created
  private final Instant instant;

  // measurements corresponding to this symptom
  private final Collection<Measurement> metrics;

  public Symptom(String symptomName, Instant instant, Collection<Measurement> metrics) {
    this.name = symptomName;
    this.instant = instant;
    this.metrics = new ArrayList<>(metrics);
  }

  public String getName() {
    return name;
  }

  public Instant getInstant() {
    return instant;
  }

  public Collection<Measurement> getMetrics() {
    return Collections.unmodifiableCollection(metrics);
  }

  @Override
  public String toString() {
    return "Symptom{" +
        "name='" + name + '\'' +
        ", instant=" + instant +
        ", metrics count=" + metrics.size() +
        '}';
  }
}

