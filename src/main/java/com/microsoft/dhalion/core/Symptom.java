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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link Symptom} identifies an anomaly or a potential health issue in a specific component of a
 * distributed application. For e.g. identification of irregular processing latency.
 */
public class Symptom {
  private static final AtomicInteger idGenerator = new AtomicInteger(1);

  // symptom identifier
  private final String type;

  // unique identifier of this instance
  private final int id;

  // instant when this symptom was created
  private final Instant instant;

  // cause identifiers
  private final Collection<String> causeIds;

  public Symptom(String symptomType, Instant instant, Collection<String> causeIds) {
    this(idGenerator.incrementAndGet(), symptomType, instant, causeIds);
  }

  public Symptom(int id, String symptomType, Instant instant, Collection<String> causeIds) {
    this.type = symptomType;
    this.instant = instant;
    this.causeIds = new ArrayList<>(causeIds);
    this.id = id;
  }

  public int id() {
    return id;
  }

  public String type() {
    return type;
  }

  public Instant instant() {
    return instant;
  }

  public Collection<String> causeIds() {
    return causeIds;
  }

  @Override
  public String toString() {
    return "Symptom{" +
        "type=" + type +
        ", id=" + id +
        ", instant=" + instant +
        ", causeIds=" + causeIds +
        '}';
  }
}

