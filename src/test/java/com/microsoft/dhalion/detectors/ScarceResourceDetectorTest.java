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

public class ScarceResourceDetectorTest {
  @Test
  public void returnsTrueIfCpuDemandHigh() {
    PolicyConfig policyConf = new PolicyConfig("policy", Collections.emptyMap());
    ScarceCpuDetector detector = new ScarceCpuDetector(policyConf);
    assertTrue(detector.evaluate("", 10, 15));
  }

  @Test
  public void returnsFalseIfCpuDemandLow() {
    PolicyConfig policyConf = new PolicyConfig("policy", Collections.emptyMap());
    ScarceCpuDetector detector = new ScarceCpuDetector(policyConf);
    assertFalse(detector.evaluate("", 10, 10));
  }

  @Test
  public void returnsTrueIfMemDemandHigh() {
    PolicyConfig policyConf = new PolicyConfig("policy", Collections.emptyMap());
    ScarceMemoryDetector detector = new ScarceMemoryDetector(policyConf);
    assertTrue(detector.evaluate("", 10, 15));
  }

  @Test
  public void returnsFalseIfMemDemandLow() {
    PolicyConfig policyConf = new PolicyConfig("policy", Collections.emptyMap());
    ScarceMemoryDetector detector = new ScarceMemoryDetector(policyConf);
    assertFalse(detector.evaluate("", 10, 10));
  }

  @Test
  public void validateMemThresholdConfig() {
    Map<String, Object> map
        = Collections.singletonMap(ScarceMemoryDetector.CONFIG_KEY_PREFIX + THRESHOLD_RATIO_CONFIG_KEY, 5.0);

    PolicyConfig policyConf = new PolicyConfig("policy", map);
    ScarceMemoryDetector detector = new ScarceMemoryDetector(policyConf);
    assertTrue(detector.evaluate("", 10, 50));
    assertFalse(detector.evaluate("", 10, 49));
  }

  @Test
  public void validateCpuThresholdConfig() {
    Map<String, Object> map
        = Collections.singletonMap(ScarceCpuDetector.CONFIG_KEY_PREFIX + THRESHOLD_RATIO_CONFIG_KEY, 5.0);

    PolicyConfig policyConf = new PolicyConfig("policy", map);
    ScarceCpuDetector detector = new ScarceCpuDetector(policyConf);
    assertTrue(detector.evaluate("", 10, 50));
    assertFalse(detector.evaluate("", 10, 49));
  }
}