/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.policy;

import com.microsoft.dhalion.api.IDetector;
import com.microsoft.dhalion.api.IDiagnoser;
import com.microsoft.dhalion.api.IResolver;
import com.microsoft.dhalion.api.ISensor;
import com.microsoft.dhalion.policy.HealthPolicyImpl.ClockTimeProvider;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class HealthPolicyImplTest {
  @Test
  public void testRegisterStages() {
    ISensor sensor = mock(ISensor.class);
    IDetector detector = mock(IDetector.class);
    IResolver resolver = mock(IResolver.class);
    IDiagnoser diagnoser = mock(IDiagnoser.class);

    HealthPolicyImpl policy = new HealthPolicyImpl();
    policy.registerSensors(sensor);
    policy.registerDetectors(detector);
    policy.registerDiagnosers(diagnoser);
    policy.registerResolvers(resolver);

    verify(sensor, times(1)).initialize();
    verify(detector, times(1)).initialize();
    verify(diagnoser, times(1)).initialize();
    verify(resolver, times(1)).initialize();
  }

  @Test
  public void testInitialize() {
    ISensor sensor = mock(ISensor.class);
    IDetector detector = mock(IDetector.class);
    IResolver resolver = mock(IResolver.class);
    IDiagnoser diagnoser = mock(IDiagnoser.class);

    HealthPolicyImpl policy = new HealthPolicyImpl();
    policy.initialize(Collections.singletonList(sensor),
        Collections.singletonList(detector),
        Collections.singletonList(diagnoser),
        Collections.singletonList(resolver));

    verify(sensor, times(1)).initialize();
    verify(detector, times(1)).initialize();
    verify(diagnoser, times(1)).initialize();
    verify(resolver, times(1)).initialize();
  }

  @Test
  public void testGetDelay() {
    HealthPolicyImpl policy = new HealthPolicyImpl();
    policy.setPolicyExecutionInterval(Duration.ofMillis(100));

    TestClock testClock = new TestClock();
    policy.clock = testClock;
    testClock.timestamp = 12345;

    // first execution should start with 0 delay
    Duration delay = policy.getDelay();
    assertTrue(delay.isZero());

    policy.executeResolvers(null);
    delay = policy.getDelay();
    assertEquals(100, delay.toMillis());

    // one time delay overrides original
    policy.setOneTimeDelay(Duration.ofMillis(10));
    delay = policy.getDelay();
    assertEquals(10, delay.toMillis());

    testClock.timestamp += 10;
    // new cycle should  reset one time delay
    policy.executeResolvers(null);
    delay = policy.getDelay();
    assertEquals(100, delay.toMillis());
  }

  @Test
  public void testClose() {
    ISensor sensor = mock(ISensor.class);
    IDetector detector = mock(IDetector.class);
    IResolver resolver = mock(IResolver.class);
    IDiagnoser diagnoser = mock(IDiagnoser.class);

    HealthPolicyImpl policy = new HealthPolicyImpl();
    policy.initialize(Collections.singletonList(sensor),
        Collections.singletonList(detector),
        Collections.singletonList(diagnoser),
        Collections.singletonList(resolver));

    policy.close();

    verify(sensor, times(1)).close();
    verify(detector, times(1)).close();
    verify(diagnoser, times(1)).close();
    verify(resolver, times(1)).close();
  }

  class TestClock extends ClockTimeProvider {
    long timestamp = -1;

    @Override
    Instant now() {
      return timestamp < 0 ? Instant.now() : Instant.ofEpochMilli(timestamp);
    }
  }
}
