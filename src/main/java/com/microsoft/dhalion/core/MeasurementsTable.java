/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import tech.tablesaw.api.CategoryColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.filtering.Filter;
import tech.tablesaw.util.Selection;

import static tech.tablesaw.api.QueryHelper.both;
import static tech.tablesaw.api.QueryHelper.column;
import static tech.tablesaw.api.QueryHelper.or;

//TODO thread safety

/**
 * An ordered collection of {@link Measurement}s. It provides methods to filter, query and aggregate the
 * {@link Measurement}s.
 */
public class MeasurementsTable {
  private static final String COMPONENT = SortKey.COMPONENT.name();
  private static final String INSTANCE = SortKey.INSTANCE.name();
  private static final String TIME_STAMP = SortKey.TIME_STAMP.name();
  private static final String TYPE = SortKey.TYPE.name();
  private static final String VALUE = SortKey.VALUE.name();
  private final Table measurements;
  private CategoryColumn component;
  private CategoryColumn instance;
  private CategoryColumn type;
  private LongColumn timeStamps;
  private DoubleColumn value;
  private MeasurementsTable() {
    component = new CategoryColumn(COMPONENT);
    instance = new CategoryColumn(INSTANCE);
    type = new CategoryColumn(TYPE);
    timeStamps = new LongColumn(TIME_STAMP);
    value = new DoubleColumn(VALUE);

    measurements = Table.create("Measurements");
    measurements.addColumn(component);
    measurements.addColumn(instance);
    measurements.addColumn(type);
    measurements.addColumn(timeStamps);
    measurements.addColumn(value);
  }

  private MeasurementsTable(Table table) {
    this.measurements = table;
    component = measurements.categoryColumn(COMPONENT);
    instance = measurements.categoryColumn(INSTANCE);
    type = measurements.categoryColumn(TYPE);
    timeStamps = measurements.longColumn(TIME_STAMP);
    value = measurements.doubleColumn(VALUE);
  }

  /**
   * @param measurements collections of measurements
   * @return a {@link MeasurementsTable} holding the input
   */
  public static MeasurementsTable of(Collection<Measurement> measurements) {
    MeasurementsTable table = new MeasurementsTable();
    table.addAll(measurements);
    return table;
  }

  private void addAll(Collection<Measurement> measurements) {
    measurements.forEach(measurement -> {
      component.append(measurement.component());
      instance.append(measurement.instance());
      type.append(measurement.type());
      timeStamps.append(measurement.instant().toEpochMilli());
      value.append(measurement.value());
    });
  }

  /**
   * Deletes all rows corresponding to measurements older than or recorded at the given expiration
   *
   * @param expiration timestamp
   * @return {@link MeasurementsTable} containing retained {@link Measurement}s
   */
  public MeasurementsTable expire(Instant expiration) {
    Selection s = TableUtils.filterTime(measurements, TIME_STAMP, null, expiration);
    return new MeasurementsTable(measurements.dropRows(s.toIntArrayList()));
  }

  /**
   * Retains all {@link Measurement}s with given component names.
   *
   * @param names of the components, not null
   * @return {@link MeasurementsTable} containing filtered {@link Measurement}s
   */
  public MeasurementsTable component(Collection<String> names) {
    return applyCategoryFilter(names, COMPONENT);
  }

  /**
   * @param name a component name
   * @return {@link MeasurementsTable} containing filtered {@link Measurement}s
   * @see #component(Collection)
   */
  public MeasurementsTable component(String name) {
    return component(Collections.singletonList(name));
  }

  /**
   * Retains all {@link Measurement}s with given metric type
   *
   * @param types names of the metric types, not null
   * @return {@link MeasurementsTable} containing filtered {@link Measurement}s
   */
  public MeasurementsTable type(Collection<String> types) {
    return applyCategoryFilter(types, TYPE);
  }

  /**
   * @param type a metric type
   * @return {@link MeasurementsTable} containing filtered {@link Measurement}s
   * @see #type(Collection)
   */
  public MeasurementsTable type(String type) {
    return type(Collections.singletonList(type));
  }

  /**
   * Retains all {@link Measurement}s with given instance names.
   *
   * @param names of the instances, not null
   * @return {@link MeasurementsTable} containing filtered {@link Measurement}s
   */
  public MeasurementsTable instance(Collection<String> names) {
    return applyCategoryFilter(names, INSTANCE);
  }

  /**
   * @param name instance name
   * @return {@link MeasurementsTable} containing filtered {@link Measurement}s
   * @see #instance(Collection)
   */
  public MeasurementsTable instance(String name) {
    return instance(Collections.singletonList(name));
  }

  private MeasurementsTable applyCategoryFilter(Collection<String> names, String column) {
    List<Filter> filters = names.stream().map(name -> column(column).isEqualTo(name)).collect(Collectors.toList());
    Table result = measurements.selectWhere(or(filters));
    return new MeasurementsTable(result);
  }

  /**
   * Retains all {@link Measurement}s with timestamp in the given range.
   *
   * @param oldest the oldest timestamp, null to ignore this condition
   * @param newest the newest timestamp, null to ignore this condition
   * @return {@link MeasurementsTable} containing filtered {@link Measurement}s
   */
  public MeasurementsTable between(Instant oldest, Instant newest) {
    Selection selection = TableUtils.filterTime(measurements, TIME_STAMP, oldest, newest);
    return new MeasurementsTable(measurements.selectWhere(selection));
  }

  /**
   * Retains all {@link Measurement}s created at the given timestamp.
   *
   * @param timestamp {@link Measurement} creation time.
   * @return {@link MeasurementsTable} containing filtered {@link Measurement}s
   */
  public MeasurementsTable instant(Instant timestamp) {
    return between(timestamp, timestamp);
  }

  /**
   * Retains only the {@link Measurement}s whose value is between <code>low</code> and <code>high</code>, both
   * inclusive.
   *
   * @param low  the lowest value to be retained
   * @param high the highest value to be retained
   * @return {@link MeasurementsTable} containing filtered {@link Measurement}s
   */
  public MeasurementsTable valueBetween(double low, double high) {
    Table result = measurements.selectWhere(
        both(column(VALUE).isGreaterThanOrEqualTo(low),
            column(VALUE).isLessThanOrEqualTo(high)));
    return new MeasurementsTable(result);
  }

  /**
   * @return count of {@link Measurement}s in this collection
   */
  public int size() {
    return measurements.rowCount();
  }

  /**
   * @return max of all the {@link Measurement}s values
   */
  public double max() {
    return value.max();
  }

  /**
   * @return min of all the {@link Measurement}s values
   */
  public double min() {
    return value.min();
  }

  /**
   * @return mean of all the {@link Measurement}s values
   */
  public double mean() {
    return value.mean();
  }

  /**
   * @return median of all the {@link Measurement}s values
   */
  public double median() {
    return value.median();
  }

  /**
   * @return variance of all the {@link Measurement}s values
   */
  public double variance() {
    return value.variance();
  }

  /**
   * @return sum of all the {@link Measurement}s values
   */
  public double sum() {
    return value.sum();
  }

  /**
   * @return unique components names in this collection of {@link Measurement}s
   */
  public Collection<String> uniqueComponents() {
    return TableUtils.uniqueCategory(component);
  }

  /**
   * @return unique instance names in this collection of {@link Measurement}s
   */
  public Collection<String> uniqueInstances() {
    return TableUtils.uniqueCategory(instance);
  }

  /**
   * @return unique metric types in this collection of {@link Measurement}s
   */
  public Collection<String> uniqueTypes() {
    return TableUtils.uniqueCategory(type);
  }

  /**
   * @return unique {@link Instant}s in this collection of {@link Measurement}s
   */
  public Collection<Instant> uniqueInstants() {
    return TableUtils.uniqueInstants(timeStamps);
  }

  /**
   * Sorts the {@link Measurement}s in this collection in the order of the specified keys
   *
   * @param descending false for ascending order, true for descending
   * @param sortKeys   one or more sort keys, e.g. {@link SortKey#COMPONENT}
   * @return ordered {@link Measurement}s
   */
  public MeasurementsTable sort(boolean descending, SortKey... sortKeys) {
    String[] columns = new String[sortKeys.length];
    for (int i = 0; i < sortKeys.length; i++) {
      columns[i] = sortKeys[i].name();
    }

    return new MeasurementsTable(TableUtils.sort(measurements, descending, columns));
  }

  /**
   * Retains the {@link Measurement} positioned between <code>first</code> and <code>last</code>, both inclusive,
   * positions in this collection
   *
   * @param first the lowest index {@link Measurement} to be retained
   * @param last  the highest index {@link Measurement} to be retained
   * @return {@link MeasurementsTable} containing specific {@link Measurement}s
   */
  public MeasurementsTable slice(int first, int last) {
    Table result = measurements.selectRows(first, last);
    return new MeasurementsTable(result);
  }

  /**
   * @return the first {@link Measurement}, if present
   */
  public Measurement first() {
    return get(0);
  }

  /**
   * @return the last {@link Measurement}, if present
   */
  public Measurement last() {
    return get(measurements.rowCount() - 1);
  }

  /**
   * @param n the number of measurements to return
   * @return the last n {@link Measurement}, if present
   */
  public MeasurementsTable last(int n) {
    Table result = measurements.selectRows(measurements.rowCount() - n,
        measurements.rowCount() - 1);
    return new MeasurementsTable(result);
  }

  /**
   * @return all {@link Measurement}s in this collection
   */
  public Collection<Measurement> get() {
    ArrayList<Measurement> result = new ArrayList<>();
    for (int i = 0; i < measurements.rowCount(); i++) {
      result.add(row2Obj(i));
    }
    return result;
  }

  /**
   * @param index position in the table
   * @return {@link Measurement} at the requested position
   */
  public Measurement get(int index) {
    if (index < 0 || index >= measurements.rowCount() || measurements.isEmpty()) {
      return null;
    }

    return row2Obj(index);
  }

  private Measurement row2Obj(int index) {
    return new Measurement(component.get(index),
        instance.get(index),
        type.get(index),
        Instant.ofEpochMilli(timeStamps.get(index)),
        value.get(index));
  }

  public String toStringForDebugging() {
    return measurements.print(measurements.rowCount());
  }

  public enum SortKey {
    COMPONENT, INSTANCE, TIME_STAMP, TYPE, VALUE
  }

  /**
   * Builds {@link MeasurementsTable} instance and provides ability to update it.
   */
  public static class Builder {
    private MeasurementsTable measurementsTable = new MeasurementsTable();

    public MeasurementsTable get() {
      return measurementsTable;
    }

    public void addAll(Collection<Measurement> measurements) {
      if (measurements == null) {
        return;
      }

      this.measurementsTable.addAll(measurements);
    }

    public void expireBefore(Instant expiration) {
      this.measurementsTable = measurementsTable.expire(expiration);
    }
  }
}
