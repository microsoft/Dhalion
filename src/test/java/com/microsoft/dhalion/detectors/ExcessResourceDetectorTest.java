/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.detectors;

import com.microsoft.dhalion.conf.PolicyConfig;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static com.microsoft.dhalion.detectors.ResourceAvailabilityDetector.THRESHOLD_RATIO_CONFIG_KEY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExcessResourceDetectorTest {
  @Test
  public void returnsTrueIfCpuDemandLow() {
    PolicyConfig policyConf = new PolicyConfig("policy", Collections.emptyMap());
    ExcessCpuDetector detector = new ExcessCpuDetector(policyConf);
    assertTrue(detector.evaluate("", 10, 5));
  }

  @Test
  public void returnsFalseIfCpuDemandHigh() {
    PolicyConfig policyConf = new PolicyConfig("policy", Collections.emptyMap());
    ExcessCpuDetector detector = new ExcessCpuDetector(policyConf);
    assertFalse(detector.evaluate("", 10, 6));
  }

  @Test
  public void returnsTrueIfMemDemandLow() {
    PolicyConfig policyConf = new PolicyConfig("policy", Collections.emptyMap());
    ExcessMemoryDetector detector = new ExcessMemoryDetector(policyConf);
    assertTrue(detector.evaluate("", 10, 5));
  }

  @Test
  public void returnsFalseIfMemDemandLow() {
    PolicyConfig policyConf = new PolicyConfig("policy", Collections.emptyMap());
    ExcessMemoryDetector detector = new ExcessMemoryDetector(policyConf);
    assertFalse(detector.evaluate("", 10, 10));
  }

  @Test
  public void validateMemThresholdConfig() {
    Map<String, Object> map
        = Collections.singletonMap(ExcessMemoryDetector.CONFIG_KEY_PREFIX + THRESHOLD_RATIO_CONFIG_KEY, 0.25);

    PolicyConfig policyConf = new PolicyConfig("policy", map);
    ExcessMemoryDetector detector = new ExcessMemoryDetector(policyConf);
    assertTrue(detector.evaluate("", 5, 20));
    assertFalse(detector.evaluate("", 5, 21));
  }

  @Test
  public void validateCpuThresholdConfig() {
    Map<String, Object> map
        = Collections.singletonMap(ExcessCpuDetector.CONFIG_KEY_PREFIX + THRESHOLD_RATIO_CONFIG_KEY, 0.25);

    PolicyConfig policyConf = new PolicyConfig("policy", map);
    ExcessCpuDetector detector = new ExcessCpuDetector(policyConf);
    assertTrue(detector.evaluate("", 10, 40));
    assertFalse(detector.evaluate("", 10, 41));
  }
}