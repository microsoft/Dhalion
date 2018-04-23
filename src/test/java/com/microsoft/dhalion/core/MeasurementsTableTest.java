/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.core;

import com.microsoft.dhalion.core.MeasurementsTable.Builder;
import com.microsoft.dhalion.core.MeasurementsTable.SortKey;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MeasurementsTableTest {
  private MeasurementsTable resultTable;
  private MeasurementsTable testTable;

  @Before
  public void createTestTable() {
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
    testTable = builder.get();
  }

  @Test
  public void component() {
    resultTable = testTable.component("c1");
    assertEquals(8, resultTable.size());
    resultTable.get().forEach(m -> assertEquals("c1", m.component()));

    resultTable = testTable.component("c2");
    assertEquals(8, resultTable.size());
    resultTable.get().forEach(m -> assertEquals("c2", m.component()));

    resultTable = testTable.component(Arrays.asList("c2", "c3"));
    assertEquals(16, resultTable.size());
    resultTable.get().forEach(m -> assertTrue("c2".equals(m.component()) || "c3".equals(m.component())));
  }

  @Test
  public void metric() {
    resultTable = testTable.type("m1");
    assertEquals(12, resultTable.size());
    resultTable.get().forEach(m -> assertEquals("m1", m.type()));
  }

  @Test
  public void instance() {
    resultTable = testTable.instance("i1");
    assertEquals(12, resultTable.size());
    resultTable.get().forEach(m -> assertEquals("i1", m.instance()));
  }

  @Test
  public void between() {
    Instant oldest = Instant.ofEpochMilli(60);
    Instant newest = Instant.ofEpochMilli(70);
    resultTable = testTable.between(oldest, newest);
    assertEquals(2, resultTable.size());
    resultTable.get().forEach(m -> assertTrue(60 <= m.instant().toEpochMilli()));
    resultTable.get().forEach(m -> assertTrue(70 >= m.instant().toEpochMilli()));
  }

  @Test
  public void expire() {
    Instant expiration = Instant.ofEpochMilli(70);
    resultTable = testTable.between(null, expiration);
    assertEquals(7, resultTable.size());

    resultTable = testTable.expire(expiration);
    assertEquals(17, resultTable.size());
    resultTable = resultTable.between(null, expiration);
    assertEquals(0, resultTable.size());
  }

  @Test
  public void max() {
    assertEquals(240, testTable.max(), 0.01);
  }

  @Test
  public void min() {
    assertEquals(10, testTable.min(), 0.01);
  }

  @Test
  public void mean() {
    assertEquals(125, testTable.mean(), 0.01);
  }

  @Test
  public void median() {
    assertEquals(125, testTable.median(), 0.01);
  }

  @Test
  public void variance() {
    assertEquals(600, testTable.component("c1").variance(), 0.01);
  }

  @Test
  public void get() {
    Collection<Measurement> result = testTable.get();
    assertEquals(24, result.size());
  }

  @Test
  public void sum() {
    assertEquals(3000, testTable.sum(), 0.01);
  }

  @Test
  public void size() {
    assertEquals(24, testTable.size());
  }

  @Test
  public void sort() {
    resultTable = testTable.valueBetween(80, 90);
    assertEquals(2, resultTable.size());
    assertEquals("i2", resultTable.first().instance());
    assertEquals("i1", resultTable.last().instance());

    resultTable = resultTable.sort(false, SortKey.INSTANCE);
    assertEquals(2, resultTable.size());
    assertEquals("i1", resultTable.first().instance());
    assertEquals("i2", resultTable.last().instance());

    assertEquals("c2", resultTable.first().component());
    assertEquals("c1", resultTable.last().component());

    resultTable = resultTable.sort(false, SortKey.COMPONENT);
    assertEquals("c1", resultTable.first().component());
    assertEquals("c2", resultTable.last().component());
  }

  @Test
  public void first() {
    Measurement measurement = testTable.first();
    assertEquals("c1", measurement.component());
    assertEquals("i1", measurement.instance());
    assertEquals(10, measurement.instant().toEpochMilli());
  }

  @Test
  public void last() {
    Measurement measurement = testTable.last();
    assertEquals("c3", measurement.component());
    assertEquals(240, measurement.instant().toEpochMilli());
  }

  @Test
  public void lastN() {
    MeasurementsTable measurement = testTable.last(3);
    assertEquals(3, measurement.size());

    assertEquals("c3", measurement.get(2).component());
    assertEquals("c3", measurement.get(1).component());
    assertEquals("c3", measurement.get(0).component());

    assertEquals(240, measurement.get(2).instant().toEpochMilli());
    assertEquals(230, measurement.get(1).instant().toEpochMilli());
    assertEquals(220, measurement.get(0).instant().toEpochMilli());
  }

  @Test
  public void slice() {
    assertEquals(24, testTable.size());
    Iterator<Measurement> measurements = testTable.get().iterator();
    double firstValue = measurements.next().value();
    double secondValue = measurements.next().value();

    resultTable = testTable.component("c1").slice(0, 1);
    assertEquals(2, resultTable.size());
    measurements = resultTable.get().iterator();
    assertEquals(firstValue, measurements.next().value(), 0.01);
    assertEquals(secondValue, measurements.next().value(), 0.01);

    resultTable = testTable.component("c3").slice(7, 7);
    assertEquals(1, resultTable.size());
    measurements = resultTable.get().iterator();
    assertEquals(240, measurements.next().value(), 0.01);
  }

  @Test
  public void valueBetween() {
    resultTable = testTable.valueBetween(45, 65);
    assertEquals(2, resultTable.size());
    resultTable.get().forEach(m -> assertTrue(45 <= m.value()));
    resultTable.get().forEach(m -> assertTrue(65 >= m.value()));
  }

  @Test
  public void uniqueComponents() {
    assertTrue(testTable.component(Arrays.asList("c1", "c2")).size() > 10);
    Collection<String> components = testTable.component(Arrays.asList("c1", "c2")).uniqueComponents();
    assertEquals(2, components.size());
    assertTrue(components.contains("c1"));
    assertTrue(components.contains("c2"));
  }
}