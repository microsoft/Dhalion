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
import java.time.Duration;
import java.util.logging.Logger;

/**
 * {@link ExcessCpuDetector} is a concrete implementation of {@link ResourceAvailabilityDetector}. It evaluates if
 * cpu resources are over-provisioned and excess cpu is not needed.
 * <p></p>
 * The detector creates a {@link Symptom} if free cpu is at-least {@code thresholdRatio} times the required cpu.
 */
public class ExcessCpuDetector extends ResourceAvailabilityDetector {
  private static final Logger LOG = Logger.getLogger(ExcessCpuDetector.class.getName());
  public static final String CONFIG_KEY_PREFIX = ExcessCpuDetector.class.getSimpleName();
  public static final String SYMPTOM_TYPE = ExcessCpuDetector.class.getSimpleName();

  private final double thresholdRatio;

  @Inject
  public ExcessCpuDetector(PolicyConfig policyConfig) {
    super((String) policyConfig.getConfig(CONFIG_KEY_PREFIX + FREE_METRIC_NAME_KEY),
          (String) policyConfig.getConfig(CONFIG_KEY_PREFIX + DEMAND_METRIC_NAME_KEY),
          Duration.ofMillis((Long) policyConfig.getConfig(CONFIG_KEY_PREFIX + DURATION_KEY)),
          SYMPTOM_TYPE);

    thresholdRatio = (double) policyConfig.getConfig(CONFIG_KEY_PREFIX + ".threshold.ratio", 2);
    LOG.info("Detector created: " + this.toString());
  }

  @Override
  protected boolean evaluate(String instance, double free, double demand) {
    if (free <= 0) {
      return false;
    }

    if (demand <= 0) {
      return true;
    }

    return (free / demand) > thresholdRatio;
  }

  @Override
  public String toString() {
    return "ExcessCpuDetector{" +
        "thresholdRatio=" + thresholdRatio +
        "} " + super.toString();
  }
}
