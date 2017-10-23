/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.metrics;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ComponentMetricsTest {
  @Test
  public void testComponentMetricsConstruction() {
    Map<String, InstanceMetrics> instanceMetricsMap = new HashMap<>();

    InstanceMetrics instanceMetrics = new InstanceMetrics("i1");
    addTestMetrics(instanceMetrics, "m1", 123);
    instanceMetricsMap.put("i1", instanceMetrics);
    ComponentMetrics componentMetrics = new ComponentMetrics("c1", instanceMetricsMap);

    assertEquals(1, componentMetrics.getInstanceData().size());

    instanceMetrics = new InstanceMetrics("i2");
    addTestMetrics(instanceMetrics, "m1", 321);
    componentMetrics.addInstanceMetric(instanceMetrics);
    assertEquals(2, componentMetrics.getInstanceData().size());

    try {
      componentMetrics.addInstanceMetric(instanceMetrics);
      fail("Should not allow duplicate instance");
    } catch (IllegalArgumentException e) {
    }

    assertEquals(2, componentMetrics.getInstanceData().size());
    assertEquals(1, componentMetrics.getMetricValues("i1", "m1").size());
    assertEquals(123, componentMetrics.getMetricValues("i1", "m1").get(Instant.ofEpochSecond(123)).intValue());
    assertEquals(1, componentMetrics.getMetricValues("i2", "m1").size());
    assertEquals(321, componentMetrics.getMetricValues("i2", "m1").get(Instant.ofEpochSecond(321)).intValue());

    assertNull(componentMetrics.getInstanceData("does not exist"));
  }

  @Test
  public void getSpecificMetrics() {
    Map<String, InstanceMetrics> instanceMetricsMap = new HashMap<>();

    InstanceMetrics instanceMetrics = new InstanceMetrics("i1");
    addTestMetrics(instanceMetrics, "m1", 123);
    addTestMetrics(instanceMetrics, "m2", 12);
    addTestMetrics(instanceMetrics, "m3", 1);

    instanceMetricsMap.put("i1", instanceMetrics);
    ComponentMetrics componentMetrics = new ComponentMetrics("c1", instanceMetricsMap);

    assertEquals(1, componentMetrics.getInstanceData().size());
    assertEquals(3, componentMetrics.getInstanceData().get("i1").getMetrics().size());

    ComponentMetrics componentMetrics2 = componentMetrics.getComponentMetric("m2");
    assertEquals(1, componentMetrics2.getInstanceData().size());
    assertEquals(1, componentMetrics2.getInstanceData().get("i1").getMetrics().size());
    assertEquals(12, componentMetrics2.getMetricValues("i1", "m2").get(Instant.ofEpochSecond(12)).
        intValue());


    instanceMetrics = new InstanceMetrics("i2");
    addTestMetrics(instanceMetrics, "m2", 321);
    componentMetrics.addInstanceMetric(instanceMetrics);
    assertEquals(2, componentMetrics.getInstanceData().size());
    assertEquals(1, componentMetrics.getInstanceData().get("i2").getMetrics().size());


    ComponentMetrics componentMetrics3 = componentMetrics.getComponentMetric("m2");
    assertEquals(2, componentMetrics3.getInstanceData().size());
    assertEquals(1, componentMetrics3.getInstanceData().get("i1").getMetrics().size());
    assertEquals(1, componentMetrics3.getInstanceData().get("i2").getMetrics().size());
    assertEquals(12, componentMetrics3.getMetricValues("i1", "m2").get(Instant.ofEpochSecond(12))
        .intValue());
    assertEquals(321, componentMetrics3.getMetricValues("i2", "m2").get(Instant.ofEpochSecond(321))
        .intValue());
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
    assertEquals(1, componentMetrics1.getInstanceData("i2").getMetrics().size());

    instanceMetricsMap.clear();
    instanceMetrics = new InstanceMetrics("i2");
    addTestMetrics(instanceMetrics, "m2", 321, 432);
    instanceMetricsMap.put("i2", instanceMetrics);

    ComponentMetrics componentMetrics2 = new ComponentMetrics("c2", instanceMetricsMap);

    ComponentMetrics result = ComponentMetrics.merge(componentMetrics1, componentMetrics2);
    assertEquals(2, result.getInstanceData().size());
    assertEquals(2, result.getInstanceData("i1").getMetrics().size());
    assertEquals(2, result.getInstanceData("i1").getMetrics().size());
  }

  private void addTestMetrics(InstanceMetrics instance, String metricName, int... values) {
    HashMap<Instant, Double> valueMap = new HashMap<>();
    for (int value : values) {
      valueMap.put(Instant.ofEpochSecond(value), (double) value);

    }
    instance.addMetric(metricName, valueMap);
  }
}
