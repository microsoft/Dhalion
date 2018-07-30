/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.detectors;

import com.microsoft.dhalion.api.IDetector;
import com.microsoft.dhalion.policy.PoliciesExecutor.ExecutionContext;

public class Detector implements IDetector {

  protected ExecutionContext context;

  @Override
  public void initialize(ExecutionContext context) {
    this.context = context;
  }
}
