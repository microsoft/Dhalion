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

import static org.junit.Assert.*;

public class ComponentMetricsTest {
  @Test
  public void testComponentMetricsConstruction() {
    Map<String, InstanceMetrics> instanceMetricsMap = new HashMap<>();

    InstanceMetrics instanceMetrics = new InstanceMetrics("i1");
    addTestMetrics(instanceMetrics, "m1", 123);
    instanceMetricsMap.put("i1", instanceMetrics);
    ComponentMetrics componentMetrics = new ComponentMetrics("c1", instanceMetricsMap);

    assertEquals(1, componentMetrics.getMetrics().size());

    instanceMetrics = new InstanceMetrics("i2");
    addTestMetrics(instanceMetrics, "m1", 321);
    componentMetrics.addInstanceMetric(instanceMetrics);
    assertEquals(2, componentMetrics.getMetrics().size());

    try {
      componentMetrics.addInstanceMetric(instanceMetrics);
      fail("Should not allow duplicate instance");
    } catch (IllegalArgumentException e) {
    }

    assertEquals(2, componentMetrics.getMetrics().size());
    assertEquals(1, componentMetrics.getMetricValues("i1", "m1").size());
    assertEquals(123, componentMetrics.getMetricValues("i1", "m1").get(Instant.ofEpochSecond(123)).intValue());
    assertEquals(1, componentMetrics.getMetricValues("i2", "m1").size());
    assertEquals(321, componentMetrics.getMetricValues("i2", "m1").get(Instant.ofEpochSecond(321)).intValue());

    assertNull(componentMetrics.getMetrics("does not exist"));
  }

  @Test
  public void findsInstanceWithMetricAboveLimit() {
    Map<String, InstanceMetrics> instanceMetricsMap = new HashMap<>();

    InstanceMetrics instanceMetrics = new InstanceMetrics("i1");
    addTestMetrics(instanceMetrics, "m1", 123, 345);
    instanceMetricsMap.put("i1", instanceMetrics);

    instanceMetrics = new InstanceMetrics("i2");
    addTestMetrics(instanceMetrics, "m1", 321, 765);
    instanceMetricsMap.put("i2", instanceMetrics);

    ComponentMetrics componentMetrics = new ComponentMetrics("c1", instanceMetricsMap);
    assertTrue(componentMetrics.anyInstanceAboveLimit("m1", 700));
    assertFalse(componentMetrics.anyInstanceAboveLimit("m1", 800));
  }

  @Test
  public void testMerge() {
    Map<String, InstanceMetrics> instanceMetricsMap = new HashMap<>();

    InstanceMetrics instanceMetrics = new InstanceMetrics("i1");
    addTestMetrics(instanceMetrics, "m1", 123, 234);
    addTestMetrics(instanceMetrics, "m2", 1234, 2345);
    instanceMetricsMap.put("i1", instanceMetrics);

    instanceMetrics = new InstanceMetrics("i2");
    addTestMetrics(instanceMetrics, "m1", 321, 432);
    instanceMetricsMap.put("i2", instanceMetrics);

    ComponentMetrics componentMetrics1 = new ComponentMetrics("c1", instanceMetricsMap);
    assertEquals(1, componentMetrics1.getMetrics("i2").getMetrics().size());

    instanceMetricsMap.clear();
    instanceMetrics = new InstanceMetrics("i2");
    addTestMetrics(instanceMetrics, "m2", 321, 432);
    instanceMetricsMap.put("i2", instanceMetrics);

    ComponentMetrics componentMetrics2 = new ComponentMetrics("c2", instanceMetricsMap);

    ComponentMetrics result = ComponentMetrics.merge(componentMetrics1, componentMetrics2);
    assertEquals(2, result.getMetrics().size());
    assertEquals(2, result.getMetrics("i1").getMetrics().size());
    assertEquals(2, result.getMetrics("i1").getMetrics().size());
  }

  private void addTestMetrics(InstanceMetrics instance, String metricName, int... values) {
    HashMap<Instant, Double> valueMap = new HashMap<>();
    for (int value : values) {
      valueMap.put(Instant.ofEpochSecond(value), (double) value);

    }
    instance.addMetric(metricName, valueMap);
  }
}
