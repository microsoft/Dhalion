/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.state;


import com.microsoft.dhalion.metrics.ComponentMetrics;
import com.microsoft.dhalion.metrics.MetricsStats;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MetricsStateTest {
  @Test
  public void testSnapshotConstructionWithStats() {
    ComponentMetrics metrics = new ComponentMetrics();
    metrics.addMetric("c1", "i1", "m1", 100);
    metrics.addMetric("c2", "i2", "m1", 200);

    Map<String, MetricsStats> stats = new HashMap<>();
    stats.put("c1", new MetricsStats("m1", 100, 100, 100));
    stats.put("c2", new MetricsStats("m1", 200, 200, 200));

    MetricsState snapshot = new MetricsState();
    snapshot.addMetricsAndStats(metrics, stats);

    assertEquals(2, snapshot.getMetrics().getMetrics().size());
    assertEquals(1, snapshot.getMetrics().filterByComponent("c1").getMetrics().size());
    assertEquals(1, snapshot.getMetrics().filterByComponent("c2").getMetrics().size());

    assertEquals(100, (int) snapshot.getStats("c1", "m1").getMetricAvg());
    assertEquals(200, (int) snapshot.getStats("c2", "m1").getMetricAvg());
    assertNull(snapshot.getStats("c1", "m2"));
    assertNull(snapshot.getStats("c2", "m2"));
    assertNull(snapshot.getStats("c1", "m3"));
    assertNull(snapshot.getStats("c2", "m3"));

    metrics = new ComponentMetrics();
    metrics.addMetric("c1", "i1", "m2", 300);
    metrics.addMetric("c2", "i2", "m2", 400);

    Map<String, MetricsStats> stats2 = new HashMap<>();
    stats2.put("c1", new MetricsStats("m2", 300, 300, 300));
    stats2.put("c2", new MetricsStats("m2", 400, 400, 400));

    snapshot.addMetricsAndStats(metrics, stats2);

    assertEquals(100, (int) snapshot.getStats("c1", "m1").getMetricAvg());
    assertEquals(200, (int) snapshot.getStats("c2", "m1").getMetricAvg());
    assertEquals(300, (int) snapshot.getStats("c1", "m2").getMetricAvg());
    assertEquals(400, (int) snapshot.getStats("c2", "m2").getMetricAvg());
    assertNull(snapshot.getStats("c1", "m3"));
    assertNull(snapshot.getStats("c2", "m3"));
  }
}