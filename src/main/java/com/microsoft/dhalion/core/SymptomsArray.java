/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.core;

import tech.tablesaw.api.CategoryColumn;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.ColumnReference;
import tech.tablesaw.filtering.Filter;
import tech.tablesaw.filtering.LongGreaterThanOrEqualTo;
import tech.tablesaw.filtering.LongLessThanOrEqualTo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static tech.tablesaw.api.QueryHelper.both;
import static tech.tablesaw.api.QueryHelper.column;
import static tech.tablesaw.api.QueryHelper.or;

//TODO thread safety

/**
 * An ordered collection of {@link Symptom}s. It provides methods to filter, query and aggregate the
 * {@link Symptom}s.
 */
public class SymptomsArray {
  private final Table symptoms;
  private CategoryColumn type;
  private IntColumn id;
  private CategoryColumn cause;
  private LongColumn timeStamp;

  public enum SortKey {
    ID, CAUSE, TIME_STAMP, TYPE
  }

  private static final String ID = SortKey.ID.name();
  private static final String CAUSE = SortKey.CAUSE.name();
  private static final String TIME_STAMP = SortKey.TIME_STAMP.name();
  private static final String TYPE = SortKey.TYPE.name();

  private SymptomsArray() {
    id = new IntColumn(ID);
    cause = new CategoryColumn(CAUSE);
    type = new CategoryColumn(TYPE);
    timeStamp = new LongColumn(TIME_STAMP);

    symptoms = Table.create("Symptoms");
    symptoms.addColumn(id);
    symptoms.addColumn(cause);
    symptoms.addColumn(type);
    symptoms.addColumn(timeStamp);
  }

  private SymptomsArray(Table table) {
    this.symptoms = table;
    id = symptoms.intColumn(ID);
    cause = symptoms.categoryColumn(CAUSE);
    type = symptoms.categoryColumn(TYPE);
    timeStamp = symptoms.longColumn(TIME_STAMP);
  }

  private void addAll(Collection<Symptom> symptoms) {
    symptoms.forEach(symptom -> {
      symptom.causeIds().forEach(causeId -> {
        id.append(symptom.id());
        cause.append(causeId);
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
    Table result = symptoms.selectWhere(column(ID).isEqualTo(id));
    return new SymptomsArray(result);
  }

  /**
   * Retains all {@link Symptom}s with given symptom type
   *
   * @param types names of the symptom types, not null
   * @return {@link SymptomsArray} containing filtered {@link Symptom}s
   */
  public SymptomsArray type(Collection<String> types) {
    return applyCategoryFilter(types, TYPE);
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
   * Retains all {@link Symptom}s with given cause ids.
   *
   * @param causeIds cause ids, not null
   * @return {@link SymptomsArray} containing filtered {@link Symptom}s
   */
  public SymptomsArray cause(Collection<String> causeIds) {
    return applyCategoryFilter(causeIds, CAUSE);
  }

  /**
   * @param causeId cause id
   * @return {@link SymptomsArray} containing filtered {@link Symptom}s
   * @see #cause(Collection)
   */
  public SymptomsArray cause(String causeId) {
    return cause(Collections.singletonList(causeId));
  }

  private SymptomsArray applyCategoryFilter(Collection<String> names, String column) {
    List<Filter> filters = names.stream().map(name -> column(column).isEqualTo(name)).collect(Collectors.toList());
    Table result = symptoms.selectWhere(or(filters));
    return new SymptomsArray(result);
  }

  /**
   * Retains all {@link Symptom}s with timestamp in the given range.
   *
   * @param oldest the oldest timestamp, null to ignore this condition
   * @param newest the newest timestamp, null to ignore this condition
   * @return {@link SymptomsArray} containing filtered {@link Symptom}s
   */
  public SymptomsArray between(Instant oldest, Instant newest) {
    Table result = symptoms.selectWhere(
        both(new LongGreaterThanOrEqualTo(new ColumnReference(TIME_STAMP), oldest.toEpochMilli()),
             new LongLessThanOrEqualTo(new ColumnReference(TIME_STAMP), newest.toEpochMilli())));

    return new SymptomsArray(result);
  }

  /**
   * @return count of {@link Symptom}s in this collection
   */
  public int size() {
    return symptoms.rowCount();
  }

  /**
   * @return unique symptom ids in this collection of {@link Symptom}s
   */
  public Collection<Integer> uniqueIds() {
    ArrayList<Integer> result = new ArrayList<>();
    IntColumn uniqueColumn = id.unique();
    for (int id : uniqueColumn) {
      result.add(id);
    }
    return result;
  }

  /**
   * @return unique symptom types in this collection of {@link Symptom}s
   */
  public Collection<String> uniqueTypes() {
    ArrayList<String> result = new ArrayList<>();
    CategoryColumn uniqueColumn = type.unique();
    uniqueColumn.forEach(result::add);
    return result;
  }

  /**
   * @return unique {@link Instant}s in this collection of {@link Symptom}s
   */
  public Collection<Instant> uniqueInstants() {
    ArrayList<Instant> result = new ArrayList<>();
    LongColumn uniqueColumn = timeStamp.unique();
    for (Long ts : uniqueColumn) {
      result.add(Instant.ofEpochMilli(ts));
    }
    return result;
  }

  /**
   * Sorts the {@link Symptom}s in this collection in the order of the specified keys
   *
   * @param sortKeys one or more sort keys, e.g. {@link SortKey#ID}
   * @return ordered {@link Symptom}s
   */
  public SymptomsArray sort(boolean descending, SortKey... sortKeys) {
    String[] columns = new String[sortKeys.length];
    for (int i = 0; i < sortKeys.length; i++) {
      columns[i] = sortKeys[i].name();
    }

    Table result;
    if (descending) {
      result = symptoms.sortDescendingOn(columns);
    } else {
      result = symptoms.sortAscendingOn(columns);
    }
    return new SymptomsArray(result);
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
    Table result = symptoms.selectRows(first, last);
    return new SymptomsArray(result);
  }

  /**
   * @return the first {@link Symptom}, if present
   */
  public Symptom first() {
    if (symptoms.isEmpty()) {
      return null;
    }

    Table result = symptoms.first(1);
    Collection<Symptom> measurementCollection = new SymptomsArray(result).get();
    return measurementCollection.iterator().next();
  }

  /**
   * @return the last {@link Symptom}, if present
   */
  public Symptom last() {
    if (symptoms.isEmpty()) {
      return null;
    }

    Table result = symptoms.last(1);
    Collection<Symptom> measurementCollection = new SymptomsArray(result).get();
    return measurementCollection.iterator().next();
  }

  /**
   * @return all {@link Symptom}s in this collection
   */
  public Collection<Symptom> get() {
    ArrayList<Symptom> result = new ArrayList<>();
    for (int i = 0; i < symptoms.rowCount(); i++) {
      result.add(new Symptom(id.get(i),
                             type.get(i),
                             Instant.ofEpochMilli(timeStamp.get(i)),
                             Collections.singletonList(cause.get(i))));
    }
    return result;
  }

  public String toStringForDebugging() {
    return symptoms.print(symptoms.rowCount());
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
