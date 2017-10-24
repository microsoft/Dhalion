/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.resolver;

import java.util.HashSet;
import java.util.Set;

import com.microsoft.dhalion.api.IResolver;
import com.microsoft.dhalion.common.InstanceInfo;
import com.microsoft.dhalion.diagnoser.Diagnosis;

/**
 * {@link Action} is a representation of a action taken by {@link IResolver} to fix a
 * {@link Diagnosis}
 */
public class Action {
  private Set<InstanceInfo> affectedInstances;

  private String type;

  public Action(String type) {
    this.type = type;
  }

  public Action(Set<InstanceInfo> affectedInstances, String type) {
    this.affectedInstances = affectedInstances;
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public Set<InstanceInfo> getAffectedInstances() {
    return affectedInstances;
  }

  public void setAffectedInstances(Set<InstanceInfo> affectedInstances) {
    this.affectedInstances = affectedInstances;
  }

  public Set<String> getAffectedComponents() {
    Set<String> affectedComponents = new HashSet<String>();
    for (InstanceInfo instance : affectedInstances) {
      String component = instance.getComponentName();
      if (!affectedComponents.contains(component)) {
        affectedComponents.add(component);
      }
    }
    return affectedComponents;
  }
}

