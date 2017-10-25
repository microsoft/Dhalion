/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.common;

public class DuplicateMetricException extends RuntimeException {
  public DuplicateMetricException(String component, String instance, String metric) {
    super(String.format("Metric name %s already exists for %s/%s", metric, component, instance));
  }
}
