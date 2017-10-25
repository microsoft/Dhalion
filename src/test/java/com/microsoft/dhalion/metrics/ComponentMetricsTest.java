/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.metrics;

import com.microsoft.dhalion.common.DuplicateMetricException;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ComponentMetricsTest {
  @Test
  public void testFilters() {
    InstanceMetric c1i2m1 = new InstanceMetric("c1", "i2", "m1");
    InstanceMetric c2i3m1 = new InstanceMetric("c2", "i3", "m1");
    InstanceMetric c2i4m3 = new InstanceMetric("c2", "i4", "m3");

    ComponentMetrics metrics = new ComponentMetrics();
    metrics.add(new InstanceMetric("c1", "i1", "m1"));
    metrics.add(new InstanceMetric("c1", "i1", "m2"));
    metrics.add(new InstanceMetric("c1", "i2", "m3"));
    metrics.add(c1i2m1);
    metrics.add(c2i3m1);
    metrics.add(c2i4m3);
    assertEquals(6, metrics.getMetrics().size());

    ComponentMetrics result = metrics.filterByComponent("c2");
    assertEquals(2, result.getMetrics().size());
    assertTrue(result.getMetrics().contains(c2i3m1));
    assertTrue(result.getMetrics().contains(c2i4m3));

    result = metrics.filterByComponent("c1");
    assertEquals(4, result.getMetrics().size());

    result = metrics.filterByMetric("m1");
    assertEquals(3, result.getMetrics().size());
    assertTrue(result.getMetrics().contains(c1i2m1));
    assertTrue(result.getMetrics().contains(c2i3m1));

    // test filter chaining
    result = metrics.filterByComponent("c1");
    assertEquals(4, result.getMetrics().size());
    result = result.filterByMetric("m1");
    assertEquals(2, result.getMetrics().size());
    result = result.filterByInstance("c1", "i1");
    assertEquals(1, result.getMetrics().size());
  }

  @Test
  public void testGetMetricNames() {
    ComponentMetrics metrics = new ComponentMetrics();
    metrics.add(new InstanceMetric("c1", "i1", "m1"));
    metrics.add(new InstanceMetric("c1", "i2", "m2"));
    metrics.add(new InstanceMetric("c2", "i3", "m1"));
    metrics.add(new InstanceMetric("c2", "i4", "m3"));
    assertEquals(4, metrics.getMetrics().size());

    Collection<String> names = metrics.getMetricNames();
    assertEquals(3, names.size());
    assertTrue(names.contains("m1"));
    assertTrue(names.contains("m2"));
    assertTrue(names.contains("m3"));
  }

  @Test
  public void testGetCompNames() {
    ComponentMetrics metrics = new ComponentMetrics();
    metrics.add(new InstanceMetric("c1", "i1", "m1"));
    metrics.add(new InstanceMetric("c1", "i2", "m2"));
    metrics.add(new InstanceMetric("c2", "i3", "m1"));
    metrics.add(new InstanceMetric("c2", "i4", "m3"));
    assertEquals(4, metrics.getMetrics().size());

    Collection<String> names = metrics.getComponentNames();
    assertEquals(2, names.size());
    assertTrue(names.contains("c1"));
    assertTrue(names.contains("c2"));
  }

  @Test(expected = DuplicateMetricException.class)
  public void testDuplicateErrors() {
    ComponentMetrics metrics = new ComponentMetrics();
    metrics.add(new InstanceMetric("c1", "i1", "m1"));
    metrics.add(new InstanceMetric("c1", "i1", "m1"));
  }

  @Test
  public void testMerge() {
    ComponentMetrics componentMetrics1 = new ComponentMetrics();
    componentMetrics1.add(new InstanceMetric("c1", "i1", "m1"));
    componentMetrics1.add(new InstanceMetric("c1", "i1", "m2"));
    componentMetrics1.add(new InstanceMetric("c1", "i2", "m2"));
    assertEquals(1, componentMetrics1.getComponentNames().size());
    assertEquals(2, componentMetrics1.getMetricNames().size());
    assertEquals(3, componentMetrics1.filterByComponent("c1").getMetrics().size());
    assertEquals(1, componentMetrics1.filterByMetric("m1").getMetrics().size());

    ComponentMetrics componentMetrics2 = new ComponentMetrics();
    componentMetrics2.add(new InstanceMetric("c1", "i1", "m3"));
    componentMetrics2.add(new InstanceMetric("c2", "i3", "m2"));
    componentMetrics2.add(new InstanceMetric("c3", "i4", "m2"));
    assertEquals(3, componentMetrics2.getComponentNames().size());
    assertEquals(2, componentMetrics2.getMetricNames().size());
    assertEquals(1, componentMetrics2.filterByComponent("c1").getMetrics().size());
    assertEquals(1, componentMetrics2.filterByComponent("c2").getMetrics().size());
    assertEquals(1, componentMetrics2.filterByComponent("c3").getMetrics().size());
    assertEquals(1, componentMetrics2.filterByMetric("m3").getMetrics().size());

    ComponentMetrics result = ComponentMetrics.merge(componentMetrics1, componentMetrics2);
    assertEquals(3, result.getComponentNames().size());
    assertEquals(3, result.getMetricNames().size());
    assertEquals(4, result.filterByComponent("c1").getMetrics().size());
    assertEquals(1, result.filterByComponent("c2").getMetrics().size());
    assertEquals(1, result.filterByComponent("c3").getMetrics().size());
    assertEquals(1, result.filterByMetric("m1").getMetrics().size());
    assertEquals(4, result.filterByMetric("m2").getMetrics().size());
    assertEquals(1, result.filterByMetric("m3").getMetrics().size());
  }
}
