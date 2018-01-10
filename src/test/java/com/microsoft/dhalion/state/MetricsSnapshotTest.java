/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.state;


import com.microsoft.dhalion.metrics.ComponentMetrics;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MetricsSnapshotTest {
  @Test
  public void testSnapshotConstructionWithStats() {
    ComponentMetrics metrics = new ComponentMetrics();
    metrics.addMetric("c1", "i1", "m1", 100);
    metrics.addMetric("c2", "i2", "m1", 200);

    MetricsSnapshot snapshot = new MetricsSnapshot();
    snapshot.addMetrics(metrics);

    assertEquals(2, snapshot.getMetrics().getMetrics().size());
    assertEquals(1, snapshot.getMetrics().filterByComponent("c1").getMetrics().size());
    assertEquals(1, snapshot.getMetrics().filterByComponent("c2").getMetrics().size());
  }
}