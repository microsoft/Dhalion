/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.examples;

/**
 * Metric names to be used with the Alert Policy.
 */
public enum MetricName {
  METRIC_CPU("Cpu"),
  METRIC_MEMORY("Mem");

  private String text;

  MetricName(String name) {
    this.text = name;
  }

  public String text() {
    return text;
  }

  @Override
  public String toString() {
    return text();
  }
}

