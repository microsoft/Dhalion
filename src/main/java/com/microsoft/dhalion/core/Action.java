/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.core;

import com.microsoft.dhalion.api.IResolver;

import java.time.Instant;
import java.util.Collection;

/**
 * {@link Action} is a representation of a action taken by {@link IResolver} to improve system health.
 */
public class Action extends Outcome {
  public Action(String type, Instant instant, Collection<String> assignments) {
    super(type, instant, assignments);
  }

  public Action(int id, String symptomType, Instant instant, Collection<String> assignments) {
    super(id, symptomType, instant, assignments);
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
