/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.app;

/**
 * An {@link InstanceInfo} holds information pertinent to a specific instance of a component of a
 * distributed application. For e.g. has task id of a bolt instance for a Storm or Heron topology.
 */
public class InstanceInfo {
  protected String name;

  public String getName() {
    return name;
  }
}
