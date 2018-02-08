/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.core;

import com.microsoft.dhalion.core.SymptomsArray.Builder;
import com.microsoft.dhalion.core.SymptomsArray.SortKey;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SymptomsArrayTest {
  private SymptomsArray testArray;
  private SymptomsArray resultArray;

  @Before
  public void createTestArray() {
    int[] ids = {1, 2, 3};
    String[] types = {"s1", "s2"};
    List<String> causes = Arrays.asList("c1", "c2", "c3");

    Collection<Symptom> symptoms = new ArrayList<>();

    int value = 10;
    for (int id : ids) {
      for (String type : types) {
        symptoms.add(new Symptom(id, type, Instant.ofEpochMilli(value), causes));
        value += 10;
      }
    }

    Builder builder = new Builder();
    builder.addAll(symptoms);
    testArray = builder.get();
  }

  @Test
  public void id() {
    resultArray = testArray.id(1);
    assertEquals(6, resultArray.size());
    resultArray.get().forEach(s -> assertEquals(1, s.id()));

    resultArray = testArray.id(2);
    assertEquals(6, resultArray.size());
    resultArray.get().forEach(symptom -> assertEquals(2, symptom.id()));
  }

  @Test
  public void type() {
    resultArray = testArray.type("s1");
    assertEquals(9, resultArray.size());
    resultArray.get().forEach(s -> assertEquals("s1", s.type()));

    resultArray = testArray.type(Arrays.asList("s1", "s2"));
    assertEquals(18, resultArray.size());
  }

  @Test
  public void cause() {
    resultArray = testArray.cause("c1");
    assertEquals(6, resultArray.size());
    resultArray.get().forEach(s -> assertEquals(1, s.causeIds().size()));
    resultArray.get().forEach(s -> assertEquals("c1", s.causeIds().iterator().next()));

    resultArray = testArray.cause(Arrays.asList("c1", "c2"));
    assertEquals(12, resultArray.size());
  }

  @Test
  public void between() {
    Instant oldest = Instant.ofEpochMilli(20);
    Instant newest = Instant.ofEpochMilli(30);
    resultArray = testArray.between(oldest, newest);
    assertEquals(6, resultArray.size());
    resultArray.get().forEach(s -> assertTrue(20 <= s.instant().toEpochMilli()));
    resultArray.get().forEach(s -> assertTrue(30 >= s.instant().toEpochMilli()));
  }

  @Test
  public void size() {
    assertEquals(18, testArray.size());
  }

  @Test
  public void uniqueIds() {
    assertTrue(testArray.id(1).size() > 1);
    Collection<Integer> ids = testArray.id(1).uniqueIds();
    assertEquals(1, ids.size());
    assertTrue(ids.contains(1));

    ids = testArray.uniqueIds();
    assertEquals(3, ids.size());
    assertTrue(ids.contains(1));
    assertTrue(ids.contains(2));
    assertTrue(ids.contains(3));
  }

  @Test
  public void uniqueTypes() {
    assertTrue(testArray.type("s1").size() > 1);
    Collection<String> types = testArray.type("s1").uniqueTypes();
    assertEquals(1, types.size());
    assertTrue(types.contains("s1"));

    types = testArray.uniqueTypes();
    assertEquals(2, types.size());
    assertTrue(types.contains("s1"));
    assertTrue(types.contains("s2"));
  }

  @Test
  public void sort() {
    resultArray = testArray.cause("c3").between(Instant.ofEpochMilli(20), Instant.ofEpochMilli(30));
    assertEquals(2, resultArray.size());
    assertEquals("s2", resultArray.first().type());
    assertEquals("s1", resultArray.last().type());

    resultArray = resultArray.sort(false, SortKey.TYPE);
    assertEquals(2, resultArray.size());
    assertEquals("s1", resultArray.first().type());
    assertEquals("s2", resultArray.last().type());

    assertEquals(2, resultArray.first().id());
    assertEquals(1, resultArray.last().id());

    resultArray = resultArray.sort(false, SortKey.ID);
    assertEquals(1, resultArray.first().id());
    assertEquals(2, resultArray.last().id());

    resultArray = resultArray.sort(true, SortKey.ID);
    assertEquals(2, resultArray.first().id());
    assertEquals(1, resultArray.last().id());
  }

  @Test
  public void slice() {
    resultArray = testArray.id(1);
    assertEquals(6, resultArray.size());
    Iterator<Symptom> symptoms = testArray.get().iterator();
    String firstCause = symptoms.next().causeIds().iterator().next();
    String secondCause = symptoms.next().causeIds().iterator().next();

    resultArray = resultArray.slice(0, 1);
    assertEquals(2, resultArray.size());
    symptoms = resultArray.get().iterator();
    assertEquals(firstCause, symptoms.next().causeIds().iterator().next());
    assertEquals(secondCause, symptoms.next().causeIds().iterator().next());
  }

  @Test
  public void first() {
    Symptom symptom = testArray.first();
    assertEquals("c1", symptom.causeIds().iterator().next());
    assertEquals(1, symptom.id());
    assertEquals(10, symptom.instant().toEpochMilli());
  }

  @Test
  public void last() {
    Symptom symptom = testArray.last();
    assertEquals("c3", symptom.causeIds().iterator().next());
    assertEquals(3, symptom.id());
    assertEquals(60, symptom.instant().toEpochMilli());
  }

  @Test
  public void get() {
    Collection<Symptom> result = testArray.get();
    assertEquals(18, result.size());
  }
}