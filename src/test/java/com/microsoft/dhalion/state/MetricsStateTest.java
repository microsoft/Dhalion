/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.state;


import com.microsoft.dhalion.metrics.ComponentMetrics;
import com.microsoft.dhalion.metrics.InstanceMetrics;
import com.microsoft.dhalion.metrics.MetricsStats;
import org.junit.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MetricsStateTest {

  @Test
  public void testSnapshotConstruction() {
    Map<String, InstanceMetrics> instanceMetricsMap1 = new HashMap<>();

    InstanceMetrics instanceMetrics1 = new InstanceMetrics("i1");
    addTestMetrics(instanceMetrics1, "m1", 123);
    addTestMetrics(instanceMetrics1, "m2", 223);
    addTestMetrics(instanceMetrics1, "m3", 323);

    instanceMetricsMap1.put("i1", instanceMetrics1);
    ComponentMetrics componentMetrics1 = new ComponentMetrics("c1", instanceMetricsMap1);

    Map<String, InstanceMetrics> instanceMetricsMap2 = new HashMap<>();

    InstanceMetrics instanceMetrics2 = new InstanceMetrics("i2");
    addTestMetrics(instanceMetrics2, "m1", 133);
    addTestMetrics(instanceMetrics2, "m2", 233);
    addTestMetrics(instanceMetrics2, "m3", 333);

    instanceMetricsMap2.put("i2", instanceMetrics2);
    ComponentMetrics componentMetrics2 = new ComponentMetrics("c2", instanceMetricsMap2);

    HashMap<String, ComponentMetrics> metrics = new HashMap<>();
    metrics.put("c1", componentMetrics1);
    metrics.put("c2", componentMetrics2);
    MetricsState snapshot = new MetricsState();
    snapshot.addMetrics(metrics);

    assertEquals(2, snapshot.getMetrics().size());
    assertEquals(1, metrics.get("c1").getInstanceData().size());
    assertEquals(1, metrics.get("c2").getInstanceData().size());
    assertEquals(3, metrics.get("c1").getInstanceData().get("i1").getMetrics().size());
    assertEquals(3, metrics.get("c2").getInstanceData().get("i2").getMetrics().size());

  }

  @Test
  public void testSnapshotConstructionWithStats() {
    Map<String, InstanceMetrics> instanceMetricsMap1 = new HashMap<>();

    InstanceMetrics instanceMetrics1 = new InstanceMetrics("i1");
    addTestMetrics(instanceMetrics1, "m1", 100);

    instanceMetricsMap1.put("i1", instanceMetrics1);
    ComponentMetrics componentMetrics1 = new ComponentMetrics("c1", instanceMetricsMap1);

    Map<String, InstanceMetrics> instanceMetricsMap2 = new HashMap<>();

    InstanceMetrics instanceMetrics2 = new InstanceMetrics("i2");
    addTestMetrics(instanceMetrics2, "m1", 200);

    instanceMetricsMap2.put("i2", instanceMetrics2);
    ComponentMetrics componentMetrics2 = new ComponentMetrics("c2", instanceMetricsMap2);

    HashMap<String, ComponentMetrics> metrics = new HashMap<>();
    metrics.put("c1", componentMetrics1);
    metrics.put("c2", componentMetrics2);

    Map<String, MetricsStats> stats = new HashMap<>();
    stats.put("c1", new MetricsStats("m1", 100, 100, 100));
    stats.put("c2", new MetricsStats("m1", 200, 200, 200));

    MetricsState snapshot = new MetricsState();
    snapshot.addMetricsAndStats(metrics, stats);

    assertEquals(2, snapshot.getMetrics().size());
    assertEquals(1, metrics.get("c1").getInstanceData().size());
    assertEquals(1, metrics.get("c2").getInstanceData().size());
    assertEquals(1, metrics.get("c1").getInstanceData().get("i1").getMetrics().size());
    assertEquals(1, metrics.get("c2").getInstanceData().get("i2").getMetrics().size());

    assertEquals(100, (int) snapshot.getStats("c1", "m1").getMetricAvg());
    assertEquals(200, (int) snapshot.getStats("c2", "m1").getMetricAvg());
    assertNull(snapshot.getStats("c1", "m2"));
    assertNull(snapshot.getStats("c2", "m2"));
    assertNull(snapshot.getStats("c1", "m3"));
    assertNull(snapshot.getStats("c2", "m3"));


    Map<String, InstanceMetrics> instanceMetricsMap3 = new HashMap<>();

    InstanceMetrics instanceMetrics3 = new InstanceMetrics("i1");
    addTestMetrics(instanceMetrics3, "m2", 300);

    instanceMetricsMap3.put("i1", instanceMetrics3);
    ComponentMetrics componentMetrics3 = new ComponentMetrics("c1", instanceMetricsMap3);

    Map<String, InstanceMetrics> instanceMetricsMap4 = new HashMap<>();

    InstanceMetrics instanceMetrics4 = new InstanceMetrics("i2");
    addTestMetrics(instanceMetrics4, "m2", 400);

    instanceMetricsMap4.put("i2", instanceMetrics4);
    ComponentMetrics componentMetrics4 = new ComponentMetrics("c2", instanceMetricsMap4);

    HashMap<String, ComponentMetrics> metrics2 = new HashMap<>();
    metrics2.put("c1", componentMetrics3);
    metrics2.put("c2", componentMetrics4);

    Map<String, MetricsStats> stats2 = new HashMap<>();
    stats2.put("c1", new MetricsStats("m2", 300, 300, 300));
    stats2.put("c2", new MetricsStats("m2", 400, 400, 400));

    snapshot.addMetricsAndStats(metrics2, stats2);

    assertEquals(100, (int) snapshot.getStats("c1", "m1").getMetricAvg());
    assertEquals(200, (int) snapshot.getStats("c2", "m1").getMetricAvg());
    assertEquals(300, (int) snapshot.getStats("c1", "m2").getMetricAvg());
    assertEquals(400, (int) snapshot.getStats("c2", "m2").getMetricAvg());
    assertNull(snapshot.getStats("c1", "m3"));
    assertNull(snapshot.getStats("c2", "m3"));

  }


  private void addTestMetrics(InstanceMetrics instance, String metricName, int... values) {
    HashMap<Instant, Double> valueMap = new HashMap<>();
    for (int value : values) {
      valueMap.put(Instant.ofEpochSecond(value), (double) value);

    }
    instance.addMetric(metricName, valueMap);
  }
}