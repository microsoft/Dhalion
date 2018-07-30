/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion;

public class Utils {

  public static String getCompositeName(String... names) {
    return names.length > 0 ? String.join("_", names) : "";
  }
}
