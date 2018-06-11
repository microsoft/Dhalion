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
 * An ordered collection of {@link Diagnosis}. It provides methods to filter, query and aggregate the
 * {@link Diagnosis} .
 */
public class DiagnosisTable extends OutcomeTable<Diagnosis> {
  private DiagnosisTable() {
    super("Symptoms");
  }

  private DiagnosisTable(Table table) {
    super(table);
  }

  /**
   * @param diagnosis collections of diagnosis
   * @return a {@link DiagnosisTable} holding the input
   */
  public static DiagnosisTable of(Collection<Diagnosis> diagnosis) {
    DiagnosisTable table = new DiagnosisTable();
    table.addAll(diagnosis);
    return table;
  }

  private void addAll(Collection<Diagnosis> diagnosis) {
    diagnosis.forEach(this::add);
  }

  /**
   * @param id unique diagnosis id
   * @return {@link Diagnosis} with the given id
   */
  public DiagnosisTable id(int id) {
    Table result = filterId(id);
    return new DiagnosisTable(result);
  }

  /**
   * Deletes all rows corresponding to diagnosis older than or recorded at the given expiration
   *
   * @param expiration timestamp
   * @return {@link DiagnosisTable} containing retained {@link Diagnosis}
   */
  public DiagnosisTable expire(Instant expiration) {
    return new DiagnosisTable(super.expireBefore(expiration));
  }

  /**
   * Retains all {@link Diagnosis} with given diagnosis type
   *
   * @param types names of the diagnosis types, not null
   * @return {@link DiagnosisTable} containing filtered {@link Diagnosis}
   */
  public DiagnosisTable type(Collection<String> types) {
    return new DiagnosisTable(filterType(types));
  }

  /**
   * @param type a diagnosis type
   * @return {@link DiagnosisTable} containing filtered {@link Diagnosis}
   * @see #type(Collection)
   */
  public DiagnosisTable type(String type) {
    return type(Collections.singletonList(type));
  }

  /**
   * Retains all {@link Diagnosis} with given assignment ids.
   *
   * @param assignments assignment ids, not null
   * @return {@link DiagnosisTable} containing filtered {@link Diagnosis}
   */
  public DiagnosisTable assignment(Collection<String> assignments) {
    return new DiagnosisTable(filterAssignment(assignments));
  }

  /**
   * @param assignment assignment id
   * @return {@link DiagnosisTable} containing filtered {@link Diagnosis}
   * @see #assignment(Collection)
   */
  public DiagnosisTable assignment(String assignment) {
    return assignment(Collections.singletonList(assignment));
  }

  /**
   * Retains all {@link Diagnosis} with timestamp in the given range.
   *
   * @param oldest the oldest timestamp, null to ignore this condition
   * @param newest the newest timestamp, null to ignore this condition
   * @return {@link DiagnosisTable} containing filtered {@link Diagnosis}
   */
  public DiagnosisTable between(Instant oldest, Instant newest) {
    return new DiagnosisTable(filterTime(oldest, newest));
  }

  /**
   * Sorts the {@link Diagnosis} in this collection in the order of the specified keys
   *
   * @param descending false for ascending order, true for descending
   * @param sortKeys   one or more sort keys, e.g. {@link SortKey#ID}
   * @return ordered {@link Diagnosis}
   */
  public DiagnosisTable sort(boolean descending, SortKey... sortKeys) {
    return new DiagnosisTable(sortTable(descending, sortKeys));
  }

  /**
   * Retains the {@link Diagnosis} positioned between <code>first</code> and <code>last</code>, both inclusive,
   * positions in this collection
   *
   * @param first the lowest index {@link Diagnosis} to be retained
   * @param last  the highest index {@link Diagnosis} to be retained
   * @return {@link DiagnosisTable} containing specific {@link Diagnosis}s
   */
  public DiagnosisTable slice(int first, int last) {
    return new DiagnosisTable(sliceTable(first, last));
  }

  Diagnosis row2Obj(int index) {
    return new Diagnosis(id.get(index),
                         type.get(index),
                         Instant.ofEpochMilli(timeStamp.get(index)),
                         Collections.singletonList(assignment.get(index)),
                         null);
  }


  /**
   * Builds {@link DiagnosisTable} instance and provides ability to update it.
   */
  public static class Builder {
    private DiagnosisTable diagnosisTable = new DiagnosisTable();

    public DiagnosisTable get() {
      return diagnosisTable;
    }

    public void addAll(Collection<Diagnosis> diagnosis) {
      if (diagnosis == null) {
        return;
      }

      this.diagnosisTable.addAll(diagnosis);
    }

    public void expireBefore(Instant expiration) {
      this.diagnosisTable = diagnosisTable.expire(expiration);
    }
  }
}
