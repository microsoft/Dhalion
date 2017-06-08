/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.events;

import com.microsoft.dhalion.policy.PoliciesExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class EventManager {
  private static final Logger LOG = Logger.getLogger(PoliciesExecutor.class.getName());
  private final Map<Class<?>, EventDispatcher<?>> registry = new HashMap<>();

  public synchronized <T> void addEventListener(Class<T> eventType, EventHandler<T> handler) {
    EventDispatcher<T> dispatcher = (EventDispatcher<T>) registry.get(eventType);
    if (dispatcher == null) {
      dispatcher = new EventDispatcher<>();
      registry.put(eventType, dispatcher);
    }
    dispatcher.addHandler(handler);
  }

  public <T> void onEvent(T event) {
    Class<?> key = event.getClass();
    EventDispatcher<T> dispatcher = (EventDispatcher<T>) registry.get(event.getClass());
    if (dispatcher == null) {
      LOG.info("No dispatcher registered for event");
      return;
    }

    dispatcher.onEvent(event);
  }
}
