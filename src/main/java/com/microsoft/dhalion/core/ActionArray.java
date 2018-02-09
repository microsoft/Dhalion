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
public class ActionArray extends OutcomeArray<Action> {
  private ActionArray() {
    super("Actions");
  }

  private ActionArray(Table table) {
    super(table);
  }

  /**
   * @param actions collections of actions
   * @return a {@link ActionArray} holding the input
   */
  public ActionArray of(Collection<Action> actions) {
    ActionArray array = new ActionArray();
    array.addAll(actions);
    return array;
  }

  private void addAll(Collection<Action> actions) {
    actions.forEach(action -> {
      action.assignments().forEach(assignment -> {
        id.append(action.id());
        this.assignment.append(assignment);
        type.append(action.type());
        timeStamp.append(action.instant().toEpochMilli());
      });
    });
  }

  /**
   * @param id unique action id
   * @return {@link Action}s with the given id
   */
  public ActionArray id(int id) {
    Table result = filterId(id);
    return new ActionArray(result);
  }

  /**
   * Retains all {@link Action}s with given action type
   *
   * @param types names of the action types, not null
   * @return {@link ActionArray} containing filtered {@link Action}s
   */
  public ActionArray type(Collection<String> types) {
    return new ActionArray(filterType(types));
  }

  /**
   * @param type a action type
   * @return {@link ActionArray} containing filtered {@link Action}s
   * @see #type(Collection)
   */
  public ActionArray type(String type) {
    return type(Collections.singletonList(type));
  }

  /**
   * Retains all {@link Action}s with given assignment ids.
   *
   * @param assignments assignment ids, not null
   * @return {@link ActionArray} containing filtered {@link Action}s
   */
  public ActionArray assignment(Collection<String> assignments) {
    return new ActionArray(filterAssignment(assignments));
  }

  /**
   * @param assignment assignment id
   * @return {@link ActionArray} containing filtered {@link Action}s
   * @see #assignment(Collection)
   */
  public ActionArray assignment(String assignment) {
    return assignment(Collections.singletonList(assignment));
  }

  /**
   * Retains all {@link Action}s with timestamp in the given range.
   *
   * @param oldest the oldest timestamp, null to ignore this condition
   * @param newest the newest timestamp, null to ignore this condition
   * @return {@link ActionArray} containing filtered {@link Action}s
   */
  public ActionArray between(Instant oldest, Instant newest) {
    return new ActionArray(filterTime(oldest, newest));
  }

  /**
   * Sorts the {@link Action}s in this collection in the order of the specified keys
   *
   * @param descending false for ascending order, true for descending
   * @param sortKeys   one or more sort keys, e.g. {@link SortKey#ID}
   * @return ordered {@link Action}s
   */
  public ActionArray sort(boolean descending, SortKey... sortKeys) {
    return new ActionArray(sortTable(descending, sortKeys));
  }

  /**
   * Retains the {@link Action} positioned between <code>first</code> and <code>last</code>, both inclusive,
   * positions in this collection
   *
   * @param first the lowest index {@link Action} to be retained
   * @param last  the highest index {@link Action} to be retained
   * @return {@link ActionArray} containing specific {@link Action}s
   */
  public ActionArray slice(int first, int last) {
    return new ActionArray(sliceTable(first, last));
  }

  Action row2Obj(int index) {
    return new Action(id.get(index),
                      type.get(index),
                      Instant.ofEpochMilli(timeStamp.get(index)),
                      Collections.singletonList(assignment.get(index)));
  }


  /**
   * Builds {@link ActionArray} instance and provides ability to update it.
   */
  public static class Builder {
    private final ActionArray actionsArray = new ActionArray();

    public ActionArray get() {
      return actionsArray;
    }

    public void addAll(Collection<Action> actions) {
      if (actions == null) {
        return;
      }

      this.actionsArray.addAll(actions);
    }
  }
}
