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
public abstract class Measurement {
  private final String component;
  private final String instance;
  private final String metric;
  private final long instantMillis; // UTC

  private Measurement(String component, String metricName, Instant instant) {
    this(component, null, metricName, instant);
  }

  private Measurement(String component, String instance, String metricName, Instant instant) {
    this.component = component;
    this.instance = instance;
    this.metric = metricName;
    this.instantMillis = instant.toEpochMilli();
  }

  public String component() {
    return component;
  }

  public String instance() {
    return instance;
  }

  public String metricName() {
    return metric;
  }

  public Instant instant() {
    return Instant.ofEpochMilli(instantMillis);
  }

  public static class ScalarMeasurement extends Measurement {
    private final double value;

    public ScalarMeasurement(String component, String instance, String metricName, Instant instant, double value) {
      super(component, instance, metricName, instant);
      this.value = value;
    }

    public ScalarMeasurement(String component, String metricName, Instant instant, double value) {
      super(component, metricName, instant);
      this.value = value;
    }

    public double value() {
      return value;
    }

    @Override
    public String toString() {
      return "Measurement {" +
          "component=" + component() +
          ", instance=" + instance() +
          ", metric=" + metricName() +
          ", instant=" + instant() +
          ", value=" + value +
          '}';
    }
  }

  public static class ObjMeasurement extends Measurement {
    private final Object value;

    public ObjMeasurement(String component, String instance, String metricName, Instant instant, Object value) {
      super(component, instance, metricName, instant);
      this.value = value;
    }

    public ObjMeasurement(String component, String metricName, Instant instant, Object value) {
      super(component, metricName, instant);
      this.value = value;
    }

    public Object value() {
      return value;
    }
  }
}