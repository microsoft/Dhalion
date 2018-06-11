/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.core;

import tech.tablesaw.api.CategoryColumn;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.ColumnReference;
import tech.tablesaw.filtering.Filter;
import tech.tablesaw.filtering.LongGreaterThanOrEqualTo;
import tech.tablesaw.filtering.LongLessThanOrEqualTo;
import tech.tablesaw.util.Selection;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static tech.tablesaw.api.QueryHelper.allOf;

class TableUtils {
  static Table sort(Table table, boolean descending, String[] columns) {
    Table result;
    if (descending) {
      result = table.sortDescendingOn(columns);
    } else {
      result = table.sortAscendingOn(columns);
    }

    return result;
  }

  static Selection filterTime(Table table, String column, Instant oldest, Instant newest) {
    List<Filter> filters = new ArrayList<>();

    if (oldest != null) {
      filters.add(new LongGreaterThanOrEqualTo(new ColumnReference(column), oldest.toEpochMilli()));
    }
    if (newest != null) {
      filters.add(new LongLessThanOrEqualTo(new ColumnReference(column), newest.toEpochMilli()));
    }

    if (filters.isEmpty()) {
      throw new IllegalArgumentException();
    }

    return allOf(filters).apply(table);
  }

  static Collection<Instant> uniqueInstants(LongColumn timeStamps) {
    ArrayList<Instant> result = new ArrayList<>();
    LongColumn uniqueColumn = timeStamps.unique();
    for (Long ts : uniqueColumn) {
      result.add(Instant.ofEpochMilli(ts));
    }
    return result;
  }

  static Collection<String> uniqueCategory(CategoryColumn column) {
    ArrayList<String> result = new ArrayList<>();
    CategoryColumn uniqueColumn = column.unique();
    uniqueColumn.forEach(result::add);
    return result;
  }
}
