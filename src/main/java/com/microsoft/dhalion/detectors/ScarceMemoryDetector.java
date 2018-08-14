/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.detectors;

import com.microsoft.dhalion.conf.PolicyConfig;
import com.microsoft.dhalion.core.Symptom;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * {@link ScarceMemoryDetector} is a concrete implementation of {@link ResourceAvailabilityDetector}. It evaluates if
 * available memory resources are insufficient to fulfill the demand.
 * <p></p>
 * The detector creates a {@link Symptom} if ratio of free memory and required memory is below {@code thresholdRatio}.
 */
public class ScarceMemoryDetector extends ResourceAvailabilityDetector {
  private static final Logger LOG = Logger.getLogger(ScarceMemoryDetector.class.getName());
  public static final String CONFIG_KEY_PREFIX = ScarceMemoryDetector.class.getSimpleName();

  private final double thresholdRatio;

  @Inject
  public ScarceMemoryDetector(PolicyConfig policyConfig) {
    super(policyConfig, CONFIG_KEY_PREFIX, SymptomName.SCARCE_MEMORY.text());
    thresholdRatio = (double) policyConfig.getConfig(CONFIG_KEY_PREFIX + THRESHOLD_RATIO_CONFIG_KEY, 1.5);
    LOG.info("Detector created: " + this.toString());
  }

  @Override
  protected boolean evaluate(String instance, double free, double demand) {
    if (demand <= 0) {
      return false;
    }

    if (free <= 0) {
      return true;
    }

    return demand / free >= thresholdRatio;
  }

  @Override
  public String toString() {
    return "ScarceMemoryDetector{" +
        "thresholdRatio=" + thresholdRatio +
        "} " + super.toString();
  }
}
