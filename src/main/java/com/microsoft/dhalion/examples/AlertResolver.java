/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.examples;

import com.microsoft.dhalion.api.IResolver;
import com.microsoft.dhalion.core.Action;
import com.microsoft.dhalion.core.Diagnosis;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * This is an example Resolver that is used by the Alert Policy.
 * It simply prints log records with the current diagnoses.
 */
public class AlertResolver implements IResolver {

  private static final Logger LOG = Logger.getLogger(AlertResolver.class.getName());

  @Override
  public Collection<Action> resolve(Collection<Diagnosis> diagnosis) {
    diagnosis.forEach(d -> {
      LOG.info("Alert " + d.toString());
    });
    return Collections.EMPTY_LIST;
  }
}
