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
import static org.junit.Assert.assertNotNull;

public class InstanceMetricsTest {
  @Test
  public void mergesDisjointInstances() {
    Map<Instant, Double> m1Values = new HashMap<>();
    m1Values.put(Instant.ofEpochSecond(123), 123.0);
    m1Values.put(Instant.ofEpochSecond(234), 234.0);

    InstanceMetrics instanceMetrics1 = new InstanceMetrics("i1");
    instanceMetrics1.addMetric("m1", m1Values);

    Map<Instant, Double> m2Values = new HashMap<>();
    m2Values.put(Instant.ofEpochSecond(321), 321.0);
    m2Values.put(Instant.ofEpochSecond(432), 432.0);

    InstanceMetrics instanceMetrics2 = new InstanceMetrics("i1");
    instanceMetrics2.addMetric("m2", m2Values);

    InstanceMetrics mergedInstanceMetrics
        = InstanceMetrics.merge(instanceMetrics1, instanceMetrics2);

    assertEquals(2, mergedInstanceMetrics.getMetrics().size());
    assertNotNull(mergedInstanceMetrics.getMetrics().get("m1"));
    assertNotNull(mergedInstanceMetrics.getMetrics().get("m2"));
    assertEquals(2, mergedInstanceMetrics.getMetrics().get("m1").size());
    assertEquals(2, mergedInstanceMetrics.getMetrics().get("m2").size());
    assertEquals(123, mergedInstanceMetrics.getMetrics().get("m1").get(Instant.ofEpochSecond(123)).intValue());
    assertEquals(432, mergedInstanceMetrics.getMetrics().get("m2").get(Instant.ofEpochSecond(432)).intValue());
  }

  @Test(expected = IllegalArgumentException.class)
  public void failsOnDuplicateMetricMerge() {
    InstanceMetrics instanceMetrics1 = new InstanceMetrics("i1");
    instanceMetrics1.addMetric("m1", new HashMap<>());
    InstanceMetrics instanceMetrics2 = new InstanceMetrics("i1");
    instanceMetrics2.addMetric("m1", new HashMap<>());
    InstanceMetrics.merge(instanceMetrics1, instanceMetrics2);
  }
}
