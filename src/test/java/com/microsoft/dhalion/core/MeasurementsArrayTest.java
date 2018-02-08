/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.core;

import com.microsoft.dhalion.core.MeasurementsArray.Builder;
import com.microsoft.dhalion.core.MeasurementsArray.SortKey;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MeasurementsArrayTest {
  private MeasurementsArray resultArray;
  private MeasurementsArray testArray;

  @Before
  public void createTestArray() {
    String[] components = {"c1", "c2", "c3"};
    String[] instances = {"i1", "i2"};
    String[] metrics = {"m1", "m2"};

    Collection<Measurement> measurements = new ArrayList<>();

    int value = 10;
    for (String component : components) {
      for (String instance : instances) {
        for (String metric : metrics) {
          measurements.add(new Measurement(component, instance, metric, Instant.ofEpochMilli(value), value));
          value += 10;
          measurements.add(new Measurement(component, instance, metric, Instant.ofEpochMilli(value), value));
          value += 10;
        }
      }
    }

    Builder builder = new Builder();
    builder.addAll(measurements);
    testArray = builder.get();
  }

  @Test
  public void component() {
    resultArray = testArray.component("c1");
    assertEquals(8, resultArray.size());
    resultArray.get().forEach(m -> assertEquals("c1", m.component()));

    resultArray = testArray.component("c2");
    assertEquals(8, resultArray.size());
    resultArray.get().forEach(m -> assertEquals("c2", m.component()));

    resultArray = testArray.component(Arrays.asList("c2", "c3"));
    assertEquals(16, resultArray.size());
    resultArray.get().forEach(m -> assertTrue("c2".equals(m.component()) || "c3".equals(m.component())));
  }

  @Test
  public void metric() {
    resultArray = testArray.type("m1");
    assertEquals(12, resultArray.size());
    resultArray.get().forEach(m -> assertEquals("m1", m.type()));
  }

  @Test
  public void instance() {
    resultArray = testArray.instance("i1");
    assertEquals(12, resultArray.size());
    resultArray.get().forEach(m -> assertEquals("i1", m.instance()));
  }

  @Test
  public void between() {
    Instant oldest = Instant.ofEpochMilli(60);
    Instant newest = Instant.ofEpochMilli(70);
    resultArray = testArray.between(oldest, newest);
    assertEquals(2, resultArray.size());
    resultArray.get().forEach(m -> assertTrue(60 <= m.instant().toEpochMilli()));
    resultArray.get().forEach(m -> assertTrue(70 >= m.instant().toEpochMilli()));
  }

  @Test
  public void max() {
    assertEquals(240, testArray.max(), 0.01);
  }

  @Test
  public void min() {
    assertEquals(10, testArray.min(), 0.01);
  }

  @Test
  public void mean() {
    assertEquals(125, testArray.mean(), 0.01);
  }

  @Test
  public void median() {
    assertEquals(125, testArray.median(), 0.01);
  }

  @Test
  public void variance() {
    assertEquals(600, testArray.component("c1").variance(), 0.01);
  }

  @Test
  public void get() {
    Collection<Measurement> result = testArray.get();
    assertEquals(24, result.size());
  }

  @Test
  public void sum() {
    assertEquals(3000, testArray.sum(), 0.01);
  }

  @Test
  public void size() {
    assertEquals(24, testArray.size());
  }

  @Test
  public void sort() {
    resultArray = testArray.valueBetween(80, 90);
    assertEquals(2, resultArray.size());
    assertEquals("i2", resultArray.first().instance());
    assertEquals("i1", resultArray.last().instance());

    resultArray = resultArray.sort(false, SortKey.INSTANCE);
    assertEquals(2, resultArray.size());
    assertEquals("i1", resultArray.first().instance());
    assertEquals("i2", resultArray.last().instance());

    assertEquals("c2", resultArray.first().component());
    assertEquals("c1", resultArray.last().component());

    resultArray = resultArray.sort(false, SortKey.COMPONENT);
    assertEquals("c1", resultArray.first().component());
    assertEquals("c2", resultArray.last().component());
  }

  @Test
  public void first() {
    Measurement measurement = testArray.first();
    assertEquals("c1", measurement.component());
    assertEquals("i1", measurement.instance());
    assertEquals(10, measurement.instant().toEpochMilli());
  }

  @Test
  public void last() {
    Measurement measurement = testArray.last();
    assertEquals("c3", measurement.component());
    assertEquals(240, measurement.instant().toEpochMilli());
  }

  @Test
  public void slice() {
    assertEquals(24, testArray.size());
    Iterator<Measurement> measurements = testArray.get().iterator();
    double firstValue = measurements.next().value();
    double secondValue = measurements.next().value();

    resultArray = testArray.component("c1").slice(0, 1);
    assertEquals(2, resultArray.size());
    measurements = resultArray.get().iterator();
    assertEquals(firstValue, measurements.next().value(), 0.01);
    assertEquals(secondValue, measurements.next().value(), 0.01);

    resultArray = testArray.component("c3").slice(7, 7);
    assertEquals(1, resultArray.size());
    measurements = resultArray.get().iterator();
    assertEquals(240, measurements.next().value(), 0.01);
  }

  @Test
  public void valueBetween() {
    resultArray = testArray.valueBetween(45, 65);
    assertEquals(2, resultArray.size());
    resultArray.get().forEach(m -> assertTrue(45 <= m.value()));
    resultArray.get().forEach(m -> assertTrue(65 >= m.value()));
  }

  @Test
  public void uniqueComponents() {
    assertTrue(testArray.component(Arrays.asList("c1", "c2")).size() > 10);
    Collection<String> components = testArray.component(Arrays.asList("c1", "c2")).uniqueComponents();
    assertEquals(2, components.size());
    assertTrue(components.contains("c1"));
    assertTrue(components.contains("c2"));
  }
}