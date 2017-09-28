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
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

public class HealthPolicyImplTest {
  @Test
  public void testRegisterStages() {
    IDetector detector = mock(IDetector.class);
    IResolver resolver = mock(IResolver.class);
    IDiagnoser diagnoser = mock(IDiagnoser.class);

    HealthPolicyImpl policy = new HealthPolicyImpl();
    policy.registerDetectors(detector);
    policy.registerDiagnosers(diagnoser);
    policy.registerResolvers(resolver);

    policy.executeDetectors();
    policy.executeDiagnosers(new ArrayList<>());
    policy.executeResolver(resolver, new ArrayList<>());

    verify(detector, times(1)).detect();
    verify(diagnoser, times(1)).diagnose(anyList());
    verify(resolver, times(1)).resolve(anyList());
  }

  @Test
  public void testInitialize() {

    ArrayList<IDetector> detectors = new ArrayList<>();
    IDetector detector = mock(IDetector.class);
    detectors.add(detector);

    ArrayList<IDiagnoser> diagnosers = new ArrayList<>();
    IDiagnoser diagnoser = mock(IDiagnoser.class);
    diagnosers.add(diagnoser);

    ArrayList<IResolver> resolvers = new ArrayList<>();
    IResolver resolver = mock(IResolver.class);
    resolvers.add(resolver);

    HealthPolicyImpl policy = new HealthPolicyImpl();
    policy.initialize(detectors, diagnosers, resolvers);

    policy.executeDetectors();
    policy.executeDiagnosers(new ArrayList<>());
    policy.executeResolver(resolver, new ArrayList<>());

    verify(detector, times(1)).detect();
    verify(diagnoser, times(1)).diagnose(anyList());
    verify(resolver, times(1)).resolve(anyList());
  }

  @Test
  public void testGetDelay() {
    HealthPolicyImpl policy = new HealthPolicyImpl();
    policy.setPolicyExecutionInterval(TimeUnit.MILLISECONDS, 100);

    TestClock testClock = new TestClock();
    policy.clock = testClock;
    testClock.timestamp = 12345;

    // first execution should start with 0 delay
    long delay = policy.getDelay(TimeUnit.MILLISECONDS);
    Assert.assertEquals(0, delay);

    policy.executeResolver(null, null);
    delay = policy.getDelay(TimeUnit.MILLISECONDS);
    Assert.assertEquals(100, delay);
    delay = policy.getDelay(TimeUnit.MILLISECONDS);
    Assert.assertEquals(100, delay);

    // one time delay overrides original
    policy.setOneTimeDelay(TimeUnit.MILLISECONDS, 10);
    delay = policy.getDelay(TimeUnit.MILLISECONDS);
    Assert.assertEquals(10, delay);


    testClock.timestamp += 10;
    // new cycle should  reset one time delay
    policy.executeResolver(null, null);
    delay = policy.getDelay(TimeUnit.MILLISECONDS);
    Assert.assertEquals(100, delay);
  }

  @Test
  public void testClose() {
    IDetector detector = mock(IDetector.class);
    IResolver resolver = mock(IResolver.class);
    IDiagnoser diagnoser = mock(IDiagnoser.class);

    HealthPolicyImpl policy = new HealthPolicyImpl();
    policy.registerDetectors(detector);
    policy.registerDiagnosers(diagnoser);
    policy.registerResolvers(resolver);

    policy.close();

    verify(detector, times(1)).close();
    verify(diagnoser, times(1)).close();
    verify(resolver, times(1)).close();
  }

  class TestClock extends ClockTimeProvider {
    long timestamp = -1;

    @Override
    long currentTimeMillis() {
      return timestamp < 0 ? System.currentTimeMillis() : timestamp;
    }
  }
}
