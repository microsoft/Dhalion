/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.app;

import java.util.Collection;

/**
 * An {@link ComponentInfo} holds information pertinent to a component of a distributed application.
 * For e.g. has aggregate count of tuples emitted by all the spout intances, {@link InstanceInfo}.
 */
public class ComponentInfo {
  protected final String name;
  protected Collection<InstanceInfo> instances;

  public ComponentInfo(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
