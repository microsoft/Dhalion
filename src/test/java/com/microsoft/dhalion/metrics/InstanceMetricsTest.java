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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InstanceMetricsTest {
  @Test
  public void testConstruction() {
    InstanceMetrics metric = new InstanceMetrics("comp", "inst", "met");
    assertEquals("comp", metric.getComponentName());
    assertEquals("inst", metric.getInstanceName());
    assertEquals("met", metric.getMetricName());
  }

  @Test
  public void testAddValue() {
    InstanceMetrics metric = new InstanceMetrics("comp", "inst", "met");
    assertEquals(0, metric.getValues().size());
    Instant before = Instant.now();
    metric.addValue(13.0);
    Instant after = Instant.now();

    assertEquals(1, metric.getValues().size());
    assertEquals(13.0, metric.getValues().values().iterator().next(), 0.0);
    Instant instant = metric.getValues().keySet().iterator().next();
    assertTrue(instant.toEpochMilli() >= before.toEpochMilli());
    assertTrue(instant.toEpochMilli() <= after.toEpochMilli());
  }

  @Test
  public void testAddValues() {
    HashMap<Instant, Double> values = new HashMap<>();

    InstanceMetrics metric = new InstanceMetrics("comp", "inst", "met");
    assertEquals(0, metric.getValues().size());

    metric.addValues(values);
    assertEquals(0, metric.getValues().size());

    values.put(Instant.now(), 10.0);
    values.put(Instant.now().plusSeconds(10), 20.0);
    values.put(Instant.now().plusSeconds(20), 30.0);

    metric.addValues(values);
    assertEquals(3, metric.getValues().size());
    assertEquals(60, metric.getValueSum(), 0.0);

    assertTrue(metric.hasValueAboveLimit(20));
    assertFalse(metric.hasValueAboveLimit(40));
  }
}
