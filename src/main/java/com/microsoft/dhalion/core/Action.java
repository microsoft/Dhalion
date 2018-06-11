/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.core;

import com.microsoft.dhalion.api.IResolver;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * {@link Action} is a representation of a action taken by {@link IResolver} to improve system health.
 */
public class Action extends Outcome {
  private final Collection<Diagnosis> diagnosis = new ArrayList<>();

  public Action(String type, Instant instant, Collection<String> assignments) {
    this(type, instant, assignments, null);
  }

  public Action(String type, Instant instant, Collection<String> assignments, Collection<Diagnosis> diagnosis) {
    super(type, instant, assignments);
    if (diagnosis != null) {
      this.diagnosis.addAll(diagnosis);
    }
  }

  Action(int id, String type, Instant instant, Collection<String> assignments, Collection<Diagnosis> diagnosis) {
    super(id, type, instant, assignments);
    if (diagnosis != null) {
      this.diagnosis.addAll(diagnosis);
    }
  }

  /**
   * @return {@link Diagnosis} referred to when this {@link Action} was created
   */
  public Collection<Diagnosis> diagnosis() {
    return Collections.unmodifiableCollection(diagnosis);
  }

  @Override
  public String toString() {
    return "Action{" +
        "id=" + id() +
        ", type=" + type() +
        ", instant=" + instant() +
        ", assignments=" + assignments() +
        '}';
  }
}
