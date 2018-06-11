/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.core;

import tech.tablesaw.api.Table;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;

//TODO thread safety

/**
 * An ordered collection of {@link Symptom}s. It provides methods to filter, query and aggregate the
 * {@link Symptom}s.
 */
public class SymptomsTable extends OutcomeTable<Symptom> {
  private SymptomsTable() {
    super("Symptoms");
  }

  private SymptomsTable(Table table) {
    super(table);
  }

  /**
   * @param symptoms collections of symptoms
   * @return a {@link SymptomsTable} holding the input
   */
  public static SymptomsTable of(Collection<Symptom> symptoms) {
    SymptomsTable table = new SymptomsTable();
    table.addAll(symptoms);
    return table;
  }

  private void addAll(Collection<Symptom> symptoms) {
    symptoms.forEach(this::add);
  }

  /**
   * Deletes all rows corresponding to {@link Symptom}s older than or recorded at the given expiration
   *
   * @param expiration timestamp
   * @return {@link SymptomsTable} containing retained {@link Symptom}s
   */
  public SymptomsTable expire(Instant expiration) {
    return new SymptomsTable(super.expireBefore(expiration));
  }

  /**
   * @param id unique symptom id
   * @return {@link Symptom}s with the given id
   */
  public SymptomsTable id(int id) {
    Table result = filterId(id);
    return new SymptomsTable(result);
  }

  /**
   * Retains all {@link Symptom}s with given symptom type
   *
   * @param types names of the symptom types, not null
   * @return {@link SymptomsTable} containing filtered {@link Symptom}s
   */
  public SymptomsTable type(Collection<String> types) {
    return new SymptomsTable(filterType(types));
  }

  /**
   * @param type a symptom type
   * @return {@link SymptomsTable} containing filtered {@link Symptom}s
   * @see #type(Collection)
   */
  public SymptomsTable type(String type) {
    return type(Collections.singletonList(type));
  }

  /**
   * Retains all {@link Symptom}s with given assignment ids.
   *
   * @param assignments assignment ids, not null
   * @return {@link SymptomsTable} containing filtered {@link Symptom}s
   */
  public SymptomsTable assignment(Collection<String> assignments) {
    return new SymptomsTable(filterAssignment(assignments));
  }

  /**
   * @param assignment assignment id
   * @return {@link SymptomsTable} containing filtered {@link Symptom}s
   * @see #assignment(Collection)
   */
  public SymptomsTable assignment(String assignment) {
    return assignment(Collections.singletonList(assignment));
  }

  /**
   * Retains all {@link Symptom}s with timestamp in the given range.
   *
   * @param oldest the oldest timestamp, null to ignore this condition
   * @param newest the newest timestamp, null to ignore this condition
   * @return {@link SymptomsTable} containing filtered {@link Symptom}s
   */
  public SymptomsTable between(Instant oldest, Instant newest) {
    return new SymptomsTable(filterTime(oldest, newest));
  }

  /**
   * @param n the number of symptoms to return
   * @return the last n {@link Symptom}, if present
   */
  public SymptomsTable last(int n) {
    return new SymptomsTable(sliceTable(size()-n, size()-1));
  }

  /**
   * Sorts the {@link Symptom}s in this collection in the order of the specified keys
   *
   * @param descending false for ascending order, true for descending
   * @param sortKeys   one or more sort keys, e.g. {@link SortKey#ID}
   * @return ordered {@link Symptom}s
   */
  public SymptomsTable sort(boolean descending, SortKey... sortKeys) {
    return new SymptomsTable(sortTable(descending, sortKeys));
  }

  /**
   * Retains the {@link Symptom} positioned between <code>first</code> and <code>last</code>, both inclusive,
   * positions in this collection
   *
   * @param first the lowest index {@link Symptom} to be retained
   * @param last  the highest index {@link Symptom} to be retained
   * @return {@link SymptomsTable} containing specific {@link Symptom}s
   */
  public SymptomsTable slice(int first, int last) {
    return new SymptomsTable(sliceTable(first, last));
  }

  Symptom row2Obj(int index) {
    return new Symptom(id.get(index),
                       type.get(index),
                       Instant.ofEpochMilli(timeStamp.get(index)),
                       Collections.singletonList(assignment.get(index)),
                       null);
  }


  /**
   * Builds {@link SymptomsTable} instance and provides ability to update it.
   */
  public static class Builder {
    private SymptomsTable symptomsTable = new SymptomsTable();

    public SymptomsTable get() {
      return symptomsTable;
    }

    public void addAll(Collection<Symptom> symptoms) {
      if (symptoms == null) {
        return;
      }

      this.symptomsTable.addAll(symptoms);
    }

    public void expireBefore(Instant expiration) {
      this.symptomsTable = symptomsTable.expire(expiration);
    }
  }
}
