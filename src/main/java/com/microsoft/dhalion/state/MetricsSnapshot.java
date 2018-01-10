/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.state;

import com.microsoft.dhalion.metrics.ComponentMetrics;

public class MetricsSnapshot {
  private ComponentMetrics metrics = new ComponentMetrics();

  public void addMetrics(ComponentMetrics newMetrics) {
    metrics = ComponentMetrics.merge(metrics, newMetrics);
  }

  public ComponentMetrics getMetrics() {
    return metrics;
  }

  public void clearMetrics() {
    metrics = new ComponentMetrics();
  }
}