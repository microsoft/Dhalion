/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.metrics;

import org.junit.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InstanceMetricsTest {
  @Test
  public void testConstruction() {
    InstanceMetrics instanceMetric = new InstanceMetrics("comp", "inst", "met");
    assertEquals("comp", instanceMetric.getComponentName());
    assertEquals("inst", instanceMetric.getInstanceName());
    assertEquals("met", instanceMetric.getMetricName());
  }

  @Test
  public void testAddValue() {
    InstanceMetrics instanceMetric = new InstanceMetrics("comp", "inst", "met");
    assertEquals(0, instanceMetric.getValues().size());
    Instant before = Instant.now();
    instanceMetric.addValue(13.0);
    Instant after = Instant.now();

    assertEquals(1, instanceMetric.getValues().size());
    assertEquals(13.0, instanceMetric.getValues().values().iterator().next(), 0.0);
    Instant instant = instanceMetric.getValues().keySet().iterator().next();
    assertTrue(instant.toEpochMilli() >= before.toEpochMilli());
    assertTrue(instant.toEpochMilli() <= after.toEpochMilli());
  }

  @Test
  public void testAddValues() {
    HashMap<Instant, Double> values = new HashMap<>();

    InstanceMetrics instanceMetric = new InstanceMetrics("comp", "inst", "met");
    assertEquals(0, instanceMetric.getValues().size());

    instanceMetric.addValues(values);
    assertEquals(0, instanceMetric.getValues().size());

    values.put(Instant.now(), 10.0);
    values.put(Instant.now().plusSeconds(10), 20.0);
    values.put(Instant.now().plusSeconds(20), 30.0);

    instanceMetric.addValues(values);
    assertEquals(3, instanceMetric.getValues().size());
    assertEquals(60, instanceMetric.getValueSum(), 0.0);

    assertTrue(instanceMetric.hasValueAboveLimit(20));
    assertFalse(instanceMetric.hasValueAboveLimit(40));
  }

  @Test
  public void testGetRecentValues() {
    HashMap<Instant, Double> values = new HashMap<>();

    InstanceMetrics instanceMetric = new InstanceMetrics("comp", "inst", "met");
    assertEquals(0, instanceMetric.getValues().size());

    instanceMetric.addValues(values);
    assertEquals(0, instanceMetric.getValues().size());

    Instant t = Instant.now();
    values.put(t, 10.0);
    values.put(t.plusSeconds(10), 20.0);
    values.put(t.plusSeconds(20), 30.0);

    instanceMetric.addValues(values);
    assertEquals(3, instanceMetric.getValues().size());
    assertEquals(60, instanceMetric.getValueSum(), 0.0);


    Map<Instant, Double> recentValues1 = instanceMetric.getMostRecentValues(1);
    assertEquals(1, recentValues1.size());
    assertTrue(recentValues1.containsKey(t.plusSeconds(20)));
    assertTrue(recentValues1.containsValue(30.0));

    Map<Instant, Double> recentValues2 = instanceMetric.getMostRecentValues(2);
    assertEquals(2, recentValues2.size());
    assertTrue(recentValues2.containsKey(t.plusSeconds(20)));
    assertTrue(recentValues2.containsKey(t.plusSeconds(10)));
    assertTrue(recentValues2.containsValue(30.0));
    assertTrue(recentValues2.containsValue(20.0));

    Map<Instant, Double> recentValues3 = instanceMetric.getMostRecentValues(3);
    assertEquals(3, recentValues3.size());
    assertEquals(instanceMetric.getValues(), recentValues3);
  }
}
