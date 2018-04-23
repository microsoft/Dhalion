/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.microsoft.dhalion.core.OutcomeTable.SortKey;
import com.microsoft.dhalion.core.SymptomsTable.Builder;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SymptomsTableTest {
  private SymptomsTable testTable;
  private SymptomsTable resultTable;

  @Before
  public void createTestTable() {
    int[] ids = {1, 2, 3};
    String[] types = {"s1", "s2"};
    List<String> attributions = Arrays.asList("c1", "c2", "c3");

    Collection<Symptom> symptoms = new ArrayList<>();

    int value = 10;
    for (int id : ids) {
      for (String type : types) {
        symptoms.add(new Symptom(id, type, Instant.ofEpochMilli(value), attributions, null));
        value += 10;
      }
    }

    Builder builder = new Builder();
    builder.addAll(symptoms);
    testTable = builder.get();
  }

  @Test
  public void id() {
    resultTable = testTable.id(1);
    assertEquals(6, resultTable.size());
    resultTable.get().forEach(s -> assertEquals(1, s.id()));

    resultTable = testTable.id(2);
    assertEquals(6, resultTable.size());
    resultTable.get().forEach(symptom -> assertEquals(2, symptom.id()));
  }

  @Test
  public void type() {
    resultTable = testTable.type("s1");
    assertEquals(9, resultTable.size());
    resultTable.get().forEach(s -> assertEquals("s1", s.type()));

    resultTable = testTable.type(Arrays.asList("s1", "s2"));
    assertEquals(18, resultTable.size());
  }

  @Test
  public void assignment() {
    resultTable = testTable.assignment("c1");
    assertEquals(6, resultTable.size());
    resultTable.get().forEach(s -> assertEquals(1, s.assignments().size()));
    resultTable.get().forEach(s -> assertEquals("c1", s.assignments().iterator().next()));

    resultTable = testTable.assignment(Arrays.asList("c1", "c2"));
    assertEquals(12, resultTable.size());
  }

  @Test
  public void between() {
    Instant oldest = Instant.ofEpochMilli(20);
    Instant newest = Instant.ofEpochMilli(30);
    resultTable = testTable.between(oldest, newest);
    assertEquals(6, resultTable.size());
    resultTable.get().forEach(s -> assertTrue(20 <= s.instant().toEpochMilli()));
    resultTable.get().forEach(s -> assertTrue(30 >= s.instant().toEpochMilli()));
  }

  @Test
  public void lastN() {
    SymptomsTable symptomsTable = testTable.last(2);
    assertEquals(2, symptomsTable.size());

    symptomsTable.get().forEach(symptom -> assertEquals(3, symptom.id()));
    assertEquals("s2", symptomsTable.get(0).type());
    assertEquals("s2", symptomsTable.get(1).type());
  }

  @Test
  public void expire() {
    Instant expiration = Instant.ofEpochMilli(20);
    resultTable = testTable.between(null, expiration);
    assertEquals(6, resultTable.size());

    resultTable = testTable.expire(expiration);
    assertEquals(12, resultTable.size());
    resultTable = resultTable.between(null, expiration);
    assertEquals(0, resultTable.size());
  }

  @Test
  public void size() {
    assertEquals(18, testTable.size());
  }

  @Test
  public void uniqueIds() {
    assertTrue(testTable.id(1).size() > 1);
    Collection<Integer> ids = testTable.id(1).uniqueIds();
    assertEquals(1, ids.size());
    assertTrue(ids.contains(1));

    ids = testTable.uniqueIds();
    assertEquals(3, ids.size());
    assertTrue(ids.contains(1));
    assertTrue(ids.contains(2));
    assertTrue(ids.contains(3));
  }

  @Test
  public void uniqueTypes() {
    assertTrue(testTable.type("s1").size() > 1);
    Collection<String> types = testTable.type("s1").uniqueTypes();
    assertEquals(1, types.size());
    assertTrue(types.contains("s1"));

    types = testTable.uniqueTypes();
    assertEquals(2, types.size());
    assertTrue(types.contains("s1"));
    assertTrue(types.contains("s2"));
  }

  @Test
  public void sort() {
    resultTable = testTable.assignment("c3").between(Instant.ofEpochMilli(20), Instant.ofEpochMilli(30));
    assertEquals(2, resultTable.size());
    assertEquals("s2", resultTable.first().type());
    assertEquals("s1", resultTable.last().type());

    resultTable = resultTable.sort(false, SortKey.TYPE);
    assertEquals(2, resultTable.size());
    assertEquals("s1", resultTable.first().type());
    assertEquals("s2", resultTable.last().type());

    assertEquals(2, resultTable.first().id());
    assertEquals(1, resultTable.last().id());

    resultTable = resultTable.sort(false, SortKey.ID);
    assertEquals(1, resultTable.first().id());
    assertEquals(2, resultTable.last().id());

    resultTable = resultTable.sort(true, SortKey.ID);
    assertEquals(2, resultTable.first().id());
    assertEquals(1, resultTable.last().id());
  }

  @Test
  public void slice() {
    resultTable = testTable.id(1);
    assertEquals(6, resultTable.size());
    Iterator<Symptom> symptoms = testTable.get().iterator();
    String assignment1 = symptoms.next().assignments().iterator().next();
    String assignment2 = symptoms.next().assignments().iterator().next();

    resultTable = resultTable.slice(0, 1);
    assertEquals(2, resultTable.size());
    symptoms = resultTable.get().iterator();
    assertEquals(assignment1, symptoms.next().assignments().iterator().next());
    assertEquals(assignment2, symptoms.next().assignments().iterator().next());
  }

  @Test
  public void first() {
    Symptom symptom = testTable.first();
    assertEquals("c1", symptom.assignments().iterator().next());
    assertEquals(1, symptom.id());
    assertEquals(10, symptom.instant().toEpochMilli());
  }

  @Test
  public void last() {
    Symptom symptom = testTable.last();
    assertEquals("c3", symptom.assignments().iterator().next());
    assertEquals(3, symptom.id());
    assertEquals(60, symptom.instant().toEpochMilli());
  }

  @Test
  public void get() {
    Collection<Symptom> result = testTable.get();
    assertEquals(18, result.size());
  }
}