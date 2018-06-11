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
import tech.tablesaw.filtering.Filter;
import tech.tablesaw.util.Selection;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static tech.tablesaw.api.QueryHelper.column;
import static tech.tablesaw.api.QueryHelper.or;

/**
 * An ordered collection of {@link Outcome} instances. This class provides methods to filter, query and aggregate the
 * {@link Outcome} instances.
 */
public abstract class OutcomeTable<T extends Outcome> {
  private final Table table;
  final CategoryColumn type;
  final IntColumn id;
  final CategoryColumn assignment;
  final LongColumn timeStamp;

  private static final String ID = SortKey.ID.name();
  private static final String ASSIGNMENT = SortKey.ASSIGNMENT.name();
  private static final String TIME_STAMP = SortKey.TIME_STAMP.name();
  private static final String TYPE = SortKey.TYPE.name();

  static final Collection<String> EMPTY_ASSIGNMENT = Collections.singletonList(CategoryColumn.MISSING_VALUE);

  public enum SortKey {
    ID, ASSIGNMENT, TIME_STAMP, TYPE
  }

  OutcomeTable(String name) {
    id = new IntColumn(ID);
    assignment = new CategoryColumn(ASSIGNMENT);
    type = new CategoryColumn(TYPE);
    timeStamp = new LongColumn(TIME_STAMP);

    table = Table.create(name);
    table.addColumn(id);
    table.addColumn(assignment);
    table.addColumn(type);
    table.addColumn(timeStamp);
  }

  OutcomeTable(Table table) {
    this.table = table;
    id = this.table.intColumn(ID);
    assignment = this.table.categoryColumn(ASSIGNMENT);
    type = this.table.categoryColumn(TYPE);
    timeStamp = this.table.longColumn(TIME_STAMP);
  }

  protected final void add(Outcome outcome) {
    Collection<String> assignments = outcome.assignments().isEmpty() ? EMPTY_ASSIGNMENT : outcome.assignments();
    assignments.forEach(assignedComponent -> {
      id.append(outcome.id());
      assignment.append(assignedComponent);
      type.append(outcome.type());
      timeStamp.append(outcome.instant().toEpochMilli());
    });
  }

  public Table expireBefore(Instant expiration) {
    Selection s = TableUtils.filterTime(table, TIME_STAMP, null, expiration);
    return table.dropRows(s.toIntArrayList());
  }

  Table filterId(int id) {
    return table.selectWhere(column(ID).isEqualTo(id));
  }

  Table filterType(Collection<String> types) {
    return applyCategoryFilter(types, TYPE);
  }

  Table filterType(String type) {
    return filterType(Collections.singletonList(type));
  }

  Table filterAssignment(Collection<String> assignments) {
    return applyCategoryFilter(assignments, ASSIGNMENT);
  }

  Table filterAssignment(String assignment) {
    return filterAssignment(Collections.singletonList(assignment));
  }

  private Table applyCategoryFilter(Collection<String> names, String column) {
    List<Filter> filters = names.stream().map(name -> column(column).isEqualTo(name)).collect(Collectors.toList());
    return table.selectWhere(or(filters));
  }

  Table filterTime(Instant oldest, Instant newest) {
    return table.selectWhere(TableUtils.filterTime(table, TIME_STAMP, oldest, newest));
  }

  /**
   * @return count of {@link Outcome} rows in this collection
   */
  public int size() {
    return table.rowCount();
  }

  /**
   * @return unique ids in this collection
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
   * @return unique {@link Outcome} types in this collection
   */
  public Collection<String> uniqueTypes() {
    return TableUtils.uniqueCategory(type);
  }

  /**
   * @return unique {@link Instant}s at which {@link Outcome} objects were created
   */
  public Collection<Instant> uniqueInstants() {
    return TableUtils.uniqueInstants(timeStamp);
  }

  Table sortTable(boolean descending, SortKey... sortKeys) {
    String[] columns = new String[sortKeys.length];
    for (int i = 0; i < sortKeys.length; i++) {
      columns[i] = sortKeys[i].name();
    }

    return TableUtils.sort(table, descending, columns);
  }

  Table sliceTable(int first, int last) {
    return table.selectRows(first, last);
  }

  /**
   * @return the first {@link Outcome} in this collection, if present
   */
  public T first() {
    return get(0);
  }

  /**
   * @return the last {@link Outcome} in this collection, if present
   */
  public T last() {
    return get(table.rowCount() - 1);
  }

  /**
   * @return all {@link Outcome} objects in this collection
   */
  public Collection<T> get() {
    ArrayList<T> result = new ArrayList<>();
    for (int i = 0; i < table.rowCount(); i++) {
      result.add(row2Obj(i));
    }
    return result;
  }

  /**
   * @param index position in the table
   * @return {@link Outcome} at the requested position
   */
  public T get(int index) {
    if (index < 0 || index >= table.rowCount() || table.isEmpty()) {
      return null;
    }

    return row2Obj(index);
  }

  abstract T row2Obj(int index);

  public String toStringForDebugging() {
    return table.print(table.rowCount());
  }
}
