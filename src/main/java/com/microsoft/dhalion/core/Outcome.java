/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.core;

import com.microsoft.dhalion.api.IDetector;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link Outcome} represent result of execution of a Dhalion phase. For e.g. {@link IDetector} phase's results in
 * {@link Symptom}s
 */
public abstract class Outcome {
  private static final AtomicInteger idGenerator = new AtomicInteger(1);

  // unique identifier of this instance
  private final int id;

  // outcome category identifier
  private final String type;

  // instant when this outcome was created
  private final Instant instant;

  // ids of objects to which this outcome can be attributed to, for e.g. slow instance's id
  private final Collection<String> assignments = new ArrayList<>();

  Outcome(String type, Instant instant, Collection<String> assignments) {
    this(idGenerator.incrementAndGet(), type, instant, assignments);
  }

  Outcome(int id, String type, Instant instant, Collection<String> assignments) {
    this.id = id;
    this.type = type;
    this.instant = instant;
    if (assignments != null) {
      this.assignments.addAll(assignments);
    }
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

  public Collection<String> assignments() {
    return assignments;
  }
}

