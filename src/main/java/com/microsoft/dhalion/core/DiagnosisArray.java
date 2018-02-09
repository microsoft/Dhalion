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
public class DiagnosisArray extends OutcomeArray<Diagnosis> {
  private DiagnosisArray() {
    super("Symptoms");
  }

  private DiagnosisArray(Table table) {
    super(table);
  }

  /**
   * @param diagnosis collections of diagnosis
   * @return a {@link DiagnosisArray} holding the input
   */
  public DiagnosisArray of(Collection<Diagnosis> diagnosis) {
    DiagnosisArray array = new DiagnosisArray();
    array.addAll(diagnosis);
    return array;
  }

  private void addAll(Collection<Diagnosis> diagnosis) {
    diagnosis.forEach(diagnoses -> {
      diagnoses.assignments().forEach(assignment -> {
        id.append(diagnoses.id());
        this.assignment.append(assignment);
        type.append(diagnoses.type());
        timeStamp.append(diagnoses.instant().toEpochMilli());
      });
    });
  }

  /**
   * @param id unique diagnosis id
   * @return {@link Diagnosis} with the given id
   */
  public DiagnosisArray id(int id) {
    Table result = filterId(id);
    return new DiagnosisArray(result);
  }

  /**
   * Retains all {@link Diagnosis} with given diagnosis type
   *
   * @param types names of the diagnosis types, not null
   * @return {@link DiagnosisArray} containing filtered {@link Diagnosis}
   */
  public DiagnosisArray type(Collection<String> types) {
    return new DiagnosisArray(filterType(types));
  }

  /**
   * @param type a diagnosis type
   * @return {@link DiagnosisArray} containing filtered {@link Diagnosis}
   * @see #type(Collection)
   */
  public DiagnosisArray type(String type) {
    return type(Collections.singletonList(type));
  }

  /**
   * Retains all {@link Diagnosis} with given assignment ids.
   *
   * @param assignments assignment ids, not null
   * @return {@link DiagnosisArray} containing filtered {@link Diagnosis}
   */
  public DiagnosisArray assignment(Collection<String> assignments) {
    return new DiagnosisArray(filterAssignment(assignments));
  }

  /**
   * @param assignment assignment id
   * @return {@link DiagnosisArray} containing filtered {@link Diagnosis}
   * @see #assignment(Collection)
   */
  public DiagnosisArray assignment(String assignment) {
    return assignment(Collections.singletonList(assignment));
  }

  /**
   * Retains all {@link Diagnosis} with timestamp in the given range.
   *
   * @param oldest the oldest timestamp, null to ignore this condition
   * @param newest the newest timestamp, null to ignore this condition
   * @return {@link DiagnosisArray} containing filtered {@link Diagnosis}
   */
  public DiagnosisArray between(Instant oldest, Instant newest) {
    return new DiagnosisArray(filterTime(oldest, newest));
  }

  /**
   * Sorts the {@link Diagnosis} in this collection in the order of the specified keys
   *
   * @param descending false for ascending order, true for descending
   * @param sortKeys   one or more sort keys, e.g. {@link SortKey#ID}
   * @return ordered {@link Diagnosis}
   */
  public DiagnosisArray sort(boolean descending, SortKey... sortKeys) {
    return new DiagnosisArray(sortTable(descending, sortKeys));
  }

  /**
   * Retains the {@link Diagnosis} positioned between <code>first</code> and <code>last</code>, both inclusive,
   * positions in this collection
   *
   * @param first the lowest index {@link Diagnosis} to be retained
   * @param last  the highest index {@link Diagnosis} to be retained
   * @return {@link DiagnosisArray} containing specific {@link Diagnosis}s
   */
  public DiagnosisArray slice(int first, int last) {
    return new DiagnosisArray(sliceTable(first, last));
  }

  Diagnosis row2Obj(int index) {
    return new Diagnosis(id.get(index),
                         type.get(index),
                         Instant.ofEpochMilli(timeStamp.get(index)),
                         Collections.singletonList(assignment.get(index)));
  }


  /**
   * Builds {@link DiagnosisArray} instance and provides ability to update it.
   */
  public static class Builder {
    private final DiagnosisArray diagnosisArray = new DiagnosisArray();

    public DiagnosisArray get() {
      return diagnosisArray;
    }

    public void addAll(Collection<Diagnosis> diagnosis) {
      if (diagnosis == null) {
        return;
      }

      this.diagnosisArray.addAll(diagnosis);
    }
  }
}
