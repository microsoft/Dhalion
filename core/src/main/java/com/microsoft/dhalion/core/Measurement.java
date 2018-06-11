/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.core;

import java.time.Instant;

/**
 * A {@link Measurement} is value of a metric corresponding to an instance at a instant of time.
 */
public class Measurement {
  private final String component;
  private final String instance;
  private final String type;
  private final Instant instant;
  private final double value;

  public Measurement(String component, String instance, String type, Instant instant, double value) {
    this.component = component;
    this.instance = instance;
    this.type = type;
    this.instant = instant;
    this.value = value;
  }

  public String component() {
    return component;
  }

  public String instance() {
    return instance;
  }

  public String type() {
    return type;
  }

  public Instant instant() {
    return instant;
  }

  public double value() {
    return value;
  }

  @Override
  public String toString() {
    return "Measurement {" +
        "component=" + component() +
        ", instance=" + instance() +
        ", type=" + type() +
        ", instant=" + instant() +
        ", value=" + value +
        '}';
  }

  public static class ObjMeasurement extends Measurement {
    private final Object reference;

    public ObjMeasurement(String component, String instance, String metricType, Instant instant, Object value) {
      super(component, instance, metricType, instant, 0);
      this.reference = value;
    }

    public Object reference() {
      return reference;
    }
  }
}