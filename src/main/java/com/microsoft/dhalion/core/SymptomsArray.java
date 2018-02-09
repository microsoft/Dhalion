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
public class SymptomsArray extends OutcomeArray<Symptom> {
  private SymptomsArray() {
    super("Symptoms");
  }

  private SymptomsArray(Table table) {
    super(table);
  }

  /**
   * @param symptoms collections of symptoms
   * @return a {@link SymptomsArray} holding the input
   */
  public SymptomsArray of(Collection<Symptom> symptoms) {
    SymptomsArray array = new SymptomsArray();
    array.addAll(symptoms);
    return array;
  }

  private void addAll(Collection<Symptom> symptoms) {
    symptoms.forEach(symptom -> {
      symptom.assignments().forEach(assignment -> {
        id.append(symptom.id());
        this.assignment.append(assignment);
        type.append(symptom.type());
        timeStamp.append(symptom.instant().toEpochMilli());
      });
    });
  }

  /**
   * @param id unique symptom id
   * @return {@link Symptom}s with the given id
   */
  public SymptomsArray id(int id) {
    Table result = filterId(id);
    return new SymptomsArray(result);
  }

  /**
   * Retains all {@link Symptom}s with given symptom type
   *
   * @param types names of the symptom types, not null
   * @return {@link SymptomsArray} containing filtered {@link Symptom}s
   */
  public SymptomsArray type(Collection<String> types) {
    return new SymptomsArray(filterType(types));
  }

  /**
   * @param type a symptom type
   * @return {@link SymptomsArray} containing filtered {@link Symptom}s
   * @see #type(Collection)
   */
  public SymptomsArray type(String type) {
    return type(Collections.singletonList(type));
  }

  /**
   * Retains all {@link Symptom}s with given assignment ids.
   *
   * @param assignments assignment ids, not null
   * @return {@link SymptomsArray} containing filtered {@link Symptom}s
   */
  public SymptomsArray assignment(Collection<String> assignments) {
    return new SymptomsArray(filterAssignment(assignments));
  }

  /**
   * @param assignment assignment id
   * @return {@link SymptomsArray} containing filtered {@link Symptom}s
   * @see #assignment(Collection)
   */
  public SymptomsArray assignment(String assignment) {
    return assignment(Collections.singletonList(assignment));
  }

  /**
   * Retains all {@link Symptom}s with timestamp in the given range.
   *
   * @param oldest the oldest timestamp, null to ignore this condition
   * @param newest the newest timestamp, null to ignore this condition
   * @return {@link SymptomsArray} containing filtered {@link Symptom}s
   */
  public SymptomsArray between(Instant oldest, Instant newest) {
    return new SymptomsArray(filterTime(oldest, newest));
  }

  /**
   * Sorts the {@link Symptom}s in this collection in the order of the specified keys
   *
   * @param descending false for ascending order, true for descending
   * @param sortKeys   one or more sort keys, e.g. {@link SortKey#ID}
   * @return ordered {@link Symptom}s
   */
  public SymptomsArray sort(boolean descending, SortKey... sortKeys) {
    return new SymptomsArray(sortTable(descending, sortKeys));
  }

  /**
   * Retains the {@link Symptom} positioned between <code>first</code> and <code>last</code>, both inclusive,
   * positions in this collection
   *
   * @param first the lowest index {@link Symptom} to be retained
   * @param last  the highest index {@link Symptom} to be retained
   * @return {@link SymptomsArray} containing specific {@link Symptom}s
   */
  public SymptomsArray slice(int first, int last) {
    return new SymptomsArray(sliceTable(first, last));
  }

  Symptom row2Obj(int index) {
    return new Symptom(id.get(index),
                       type.get(index),
                       Instant.ofEpochMilli(timeStamp.get(index)),
                       Collections.singletonList(assignment.get(index)));
  }


  /**
   * Builds {@link SymptomsArray} instance and provides ability to update it.
   */
  public static class Builder {
    private final SymptomsArray symptomsArray = new SymptomsArray();

    public SymptomsArray get() {
      return symptomsArray;
    }

    public void addAll(Collection<Symptom> symptoms) {
      if (symptoms == null) {
        return;
      }

      this.symptomsArray.addAll(symptoms);
    }
  }
}
