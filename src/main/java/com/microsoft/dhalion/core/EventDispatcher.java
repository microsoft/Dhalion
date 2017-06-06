/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.core;

import java.util.HashSet;
import java.util.Set;

public class EventDispatcher<T> {
  Set<EventHandler<T>> handlers = new HashSet<>();

  public synchronized void addHandler(EventHandler<T> handler) {
    if (handlers.contains(handler)) {
      throw new IllegalArgumentException("Duplicate hanlder registration");
    }
    handlers.add(handler);
  }

  public void dispatch(T event) {
    handlers.forEach(x -> x.onEvent(event));
  }
}
