/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.resolver;

import com.microsoft.dhalion.api.IResolver;
import com.microsoft.dhalion.diagnoser.Diagnosis;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * {@link Action} is a representation of a action taken by {@link IResolver} to improve system health.
 */
public class Action {
  // action type
  private final String name;

  // instant when this action was created
  private final Instant instant;

  // Diagnosis relevant to this action
  private final Collection<Diagnosis> diagnosis;

  public Action(String name, Instant instant, Collection<Diagnosis> diagnosis) {
    this.name = name;
    this.instant = instant;
    this.diagnosis = new ArrayList<>(diagnosis);
  }

  public String getName() {
    return name;
  }

  public Instant getInstant() {
    return instant;
  }

  public Collection<Diagnosis> getDiagnosis() {
    return Collections.unmodifiableCollection(diagnosis);
  }

  @Override
  public String toString() {
    return "Action{" +
        "name='" + name + '\'' +
        ", instant=" + instant +
        ", diagnosis=" + diagnosis +
        '}';
  }
}
