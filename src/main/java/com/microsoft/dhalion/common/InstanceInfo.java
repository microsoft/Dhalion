/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.common;

/**
 * An {@link InstanceInfo} holds information identifying an instance of a component.
 */
public class InstanceInfo {
  // id of the component
  protected final String componentName;

  // id of the instance
  protected final String instanceName;

  public InstanceInfo(String componentName, String instanceName) {
    this.componentName = componentName;
    this.instanceName = instanceName;
  }

  public String getComponentName() {
    return componentName;
  }

  public String getInstanceName() {
    return instanceName;
  }

  @Override
  public String toString() {
    return "InstanceInfo{" +
        "componentName='" + componentName + '\'' +
        ", instanceName='" + instanceName + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    InstanceInfo that = (InstanceInfo) o;

    return componentName.equals(that.componentName) && instanceName.equals(that.instanceName);
  }

  @Override
  public int hashCode() {
    int result = componentName.hashCode();
    result = 31 * result + instanceName.hashCode();
    return result;
  }
}