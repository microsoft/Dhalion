/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */
package com.microsoft.dhalion.core;

import com.microsoft.dhalion.core.Measurement.ScalarMeasurement;
import tech.tablesaw.api.CategoryColumn;
import tech.tablesaw.api.DoubleColumn;
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
 * An ordered collection of {@link Measurement}s. It provides methods to filter, query and aggregate the
 * {@link Measurement}s.
 */
public class MeasurementsArray {
  private final Table measurements;
  CategoryColumn component;
  CategoryColumn instance;
  CategoryColumn metricName;
  LongColumn timeStamps;
  DoubleColumn value;

  public enum SortKey {
    COMPONENT, INSTANCE, TIME_STAMP, METRIC_NAME, VALUE
  }

  private static final String COMPONENT = SortKey.COMPONENT.name();
  private static final String INSTANCE = SortKey.INSTANCE.name();
  private static final String TIME_STAMP = SortKey.TIME_STAMP.name();
  private static final String METRIC_NAME = SortKey.METRIC_NAME.name();
  private static final String VALUE = SortKey.VALUE.name();

  private MeasurementsArray() {
    component = new CategoryColumn(COMPONENT);
    instance = new CategoryColumn(INSTANCE);
    metricName = new CategoryColumn(METRIC_NAME);
    timeStamps = new LongColumn(TIME_STAMP);
    value = new DoubleColumn(VALUE);

    measurements = Table.create("Measurements");
    measurements.addColumn(component);
    measurements.addColumn(instance);
    measurements.addColumn(metricName);
    measurements.addColumn(timeStamps);
    measurements.addColumn(value);
  }

  private MeasurementsArray(Table table) {
    this.measurements = table;
    component = measurements.categoryColumn(COMPONENT);
    instance = measurements.categoryColumn(INSTANCE);
    metricName = measurements.categoryColumn(METRIC_NAME);
    timeStamps = measurements.longColumn(TIME_STAMP);
    value = measurements.doubleColumn(VALUE);
  }

  private void addAll(Collection<Measurement> measurements) {
    measurements.forEach(measurement -> {
      component.append(measurement.component());
      instance.append(measurement.instance());
      metricName.append(measurement.metricName());
      timeStamps.append(measurement.instant().toEpochMilli());
      value.append(((ScalarMeasurement) measurement).value());
    });
  }

  /**
   * Retains all {@link Measurement}s with given component names.
   *
   * @param names of the components, not null
   * @return {@link Measurement} containing filtered {@link Measurement}s
   */
  public MeasurementsArray component(Collection<String> names) {
    return applyCategoryFilter(names, COMPONENT);
  }

  /**
   * @see #component(Collection)
   */
  public MeasurementsArray component(String name) {
    return component(Collections.singletonList(name));
  }

  /**
   * Retains all {@link Measurement}s with given metric name.
   *
   * @param names of the metrics, not null
   * @return {@link Measurement} containing filtered {@link Measurement}s
   */
  public MeasurementsArray metric(Collection<String> names) {
    return applyCategoryFilter(names, METRIC_NAME);
  }

  /**
   * @see #metric(Collection)
   */
  public MeasurementsArray metric(String name) {
    return metric(Collections.singletonList(name));
  }

  /**
   * Retains all {@link Measurement}s with given instance names.
   *
   * @param names of the instances, not null
   * @return {@link Measurement} containing filtered {@link Measurement}s
   */
  public MeasurementsArray instance(Collection<String> names) {
    return applyCategoryFilter(names, INSTANCE);
  }

  /**
   * @see #instance(String)
   */
  public MeasurementsArray instance(String name) {
    return instance(Collections.singletonList(name));
  }

  private MeasurementsArray applyCategoryFilter(Collection<String> names, String column) {
    List<Filter> filters = names.stream().map(name -> column(column).isEqualTo(name)).collect(Collectors.toList());
    Table result = measurements.selectWhere(or(filters));
    return new MeasurementsArray(result);
  }

  /**
   * Retains all {@link Measurement}s with timestamp in the given range.
   *
   * @param oldest the oldest timestamp, null to ignore this condition
   * @param newest the newest timestamp, null to ignore this condition
   * @return {@link MeasurementsArray} containing filtered {@link Measurement}s
   */
  public MeasurementsArray between(Instant oldest, Instant newest) {
    Table result = measurements.selectWhere(
        both(new LongGreaterThanOrEqualTo(new ColumnReference(TIME_STAMP), oldest.toEpochMilli()),
             new LongLessThanOrEqualTo(new ColumnReference(TIME_STAMP), newest.toEpochMilli())));

    return new MeasurementsArray(result);
  }

  /**
   * @return count of {@link Measurement}s in this collection
   */
  public int size() {
    return measurements.rowCount();
  }

  /**
   * @return max of all the {@link Measurement}s in this collection
   */
  public double max() {
    return value.max();
  }

  /**
   * @return min of all the {@link Measurement}s in this collection
   */
  public double min() {
    return value.min();
  }

  /**
   * @return mean of all the {@link Measurement}s in this collection
   */
  public double mean() {
    return value.mean();
  }

  /**
   * @return median of all the {@link Measurement}s in this collection
   */
  public double median() {
    return value.median();
  }

  /**
   * @return variance of all the {@link Measurement}s in this collection
   */
  public double variance() {
    return value.variance();
  }

  /**
   * Sorts the {@link Measurement}s in this collection in the order of the specified keys
   *
   * @param sortKeys one or more sort keys, e.g. {@link SortKey#COMPONENT}
   * @return ordered {@link Measurement}s
   */
  public MeasurementsArray sort(boolean descending, SortKey... sortKeys) {
    throw new UnsupportedOperationException();
  }

  /**
   * Retains the {@link Measurement} positioned between <code>first</code> and <code>last</code>, both inclusive,
   * positions in this collection
   *
   * @param first the lowest index {@link Measurement} to be retained
   * @param last  the highest index {@link Measurement} to be retained
   * @return {@link MeasurementsArray} containing specific {@link Measurement}s
   */
  public MeasurementsArray slice(int first, int last) {
    throw new UnsupportedOperationException();
  }

  /**
   * Retains only the {@link Measurement}s whose value is between <code>low</code> and <code>high</code>, both
   * inclusive.
   *
   * @param low  the lowest value to be retained
   * @param high the highest value to be retained
   * @return {@link MeasurementsArray} containing filtered {@link Measurement}s
   */
  public MeasurementsArray valueBetween(double low, double high) {
    throw new UnsupportedOperationException();
  }

  /**
   * @return all {@link Measurement}s in this collection
   */
  public Collection<Measurement> get() {
    ArrayList<Measurement> result = new ArrayList<>();
    for (int i = 0; i < measurements.rowCount(); i++) {
      result.add(new ScalarMeasurement(component.get(i),
                                       instance.get(i),
                                       metricName.get(i),
                                       Instant.ofEpochMilli(timeStamps.get(i)),
                                       value.get(i)));
    }
    return result;
  }

  /**
   * Builds {@link MeasurementsArray} instance and provides ability to update it.
   */
  public static class Builder {
    private final MeasurementsArray measurementsArray = new MeasurementsArray();

    public MeasurementsArray get() {
      return measurementsArray;
    }

    public void addAll(Collection<Measurement> measurements) {
      if (measurements == null) {
        return;
      }

      this.measurementsArray.addAll(measurements);
    }
  }
}
