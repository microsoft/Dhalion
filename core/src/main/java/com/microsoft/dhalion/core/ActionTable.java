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
 * An ordered collection of {@link Action}s. It provides methods to filter, query and aggregate the
 * {@link Action}s.
 */
public class ActionTable extends OutcomeTable<Action> {
  private ActionTable() {
    super("Actions");
  }

  private ActionTable(Table table) {
    super(table);
  }

  /**
   * @param actions collections of actions
   * @return a {@link ActionTable} holding the input
   */
  public static ActionTable of(Collection<Action> actions) {
    ActionTable table = new ActionTable();
    table.addAll(actions);
    return table;
  }

  private void addAll(Collection<Action> actions) {
    actions.forEach(this::add);
  }

  /**
   * Deletes all rows corresponding to actions older than or recorded at the given expiration
   *
   * @param expiration timestamp
   * @return {@link ActionTable} containing retained {@link Action}s
   */
  public ActionTable expire(Instant expiration) {
    return new ActionTable(super.expireBefore(expiration));
  }

  /**
   * @param id unique action id
   * @return {@link Action}s with the given id
   */
  public ActionTable id(int id) {
    Table result = filterId(id);
    return new ActionTable(result);
  }

  /**
   * Retains all {@link Action}s with given action type
   *
   * @param types names of the action types, not null
   * @return {@link ActionTable} containing filtered {@link Action}s
   */
  public ActionTable type(Collection<String> types) {
    return new ActionTable(filterType(types));
  }

  /**
   * @param type a action type
   * @return {@link ActionTable} containing filtered {@link Action}s
   * @see #type(Collection)
   */
  public ActionTable type(String type) {
    return type(Collections.singletonList(type));
  }

  /**
   * Retains all {@link Action}s with given assignment ids.
   *
   * @param assignments assignment ids, not null
   * @return {@link ActionTable} containing filtered {@link Action}s
   */
  public ActionTable assignment(Collection<String> assignments) {
    return new ActionTable(filterAssignment(assignments));
  }

  /**
   * @param assignment assignment id
   * @return {@link ActionTable} containing filtered {@link Action}s
   * @see #assignment(Collection)
   */
  public ActionTable assignment(String assignment) {
    return assignment(Collections.singletonList(assignment));
  }

  /**
   * Retains all {@link Action}s with timestamp in the given range.
   *
   * @param oldest the oldest timestamp, null to ignore this condition
   * @param newest the newest timestamp, null to ignore this condition
   * @return {@link ActionTable} containing filtered {@link Action}s
   */
  public ActionTable between(Instant oldest, Instant newest) {
    return new ActionTable(filterTime(oldest, newest));
  }

  /**
   * Sorts the {@link Action}s in this collection in the order of the specified keys
   *
   * @param descending false for ascending order, true for descending
   * @param sortKeys   one or more sort keys, e.g. {@link SortKey#ID}
   * @return ordered {@link Action}s
   */
  public ActionTable sort(boolean descending, SortKey... sortKeys) {
    return new ActionTable(sortTable(descending, sortKeys));
  }

  /**
   * Retains the {@link Action} positioned between <code>first</code> and <code>last</code>, both inclusive,
   * positions in this collection
   *
   * @param first the lowest index {@link Action} to be retained
   * @param last  the highest index {@link Action} to be retained
   * @return {@link ActionTable} containing specific {@link Action}s
   */
  public ActionTable slice(int first, int last) {
    return new ActionTable(sliceTable(first, last));
  }

  Action row2Obj(int index) {
    return new Action(id.get(index),
                      type.get(index),
                      Instant.ofEpochMilli(timeStamp.get(index)),
                      Collections.singletonList(assignment.get(index)),
                      null);
  }


  /**
   * Builds {@link ActionTable} instance and provides ability to update it.
   */
  public static class Builder {
    private ActionTable actionsTable = new ActionTable();

    public ActionTable get() {
      return actionsTable;
    }

    public void addAll(Collection<Action> actions) {
      if (actions == null) {
        return;
      }

      this.actionsTable.addAll(actions);
    }

    public void expireBefore(Instant expiration) {
      this.actionsTable = actionsTable.expire(expiration);
    }
  }
}
