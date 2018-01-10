/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.policy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.microsoft.dhalion.api.IDetector;
import com.microsoft.dhalion.api.IDiagnoser;
import com.microsoft.dhalion.api.IResolver;
import com.microsoft.dhalion.api.ISensor;
import com.microsoft.dhalion.policy.HealthPolicyImpl.ClockTimeProvider;

import com.microsoft.dhalion.state.MetricsSnapshot;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

    policy.executeDetectors(null);
    policy.executeDiagnosers(null, new HashSet<>());
    policy.executeResolver(resolver, null, new HashSet<>(), new HashSet<>());

    verify(detector, times(1)).detect(any());
    verify(diagnoser, times(1)).diagnose(any(), anySet());
    verify(resolver, times(1)).resolve(any(), anySet(), anySet());
  }

  @Test
  public void testInitialize() {

    Set<ISensor> sensors = new HashSet<>();
    ISensor sensor = mock(ISensor.class);
    sensors.add(sensor);

    Set<IDetector> detectors = new HashSet<>();
    IDetector detector = mock(IDetector.class);
    detectors.add(detector);

    Set<IDiagnoser> diagnosers = new HashSet<>();
    IDiagnoser diagnoser = mock(IDiagnoser.class);
    diagnosers.add(diagnoser);

    Set<IResolver> resolvers = new HashSet<>();
    IResolver resolver = mock(IResolver.class);
    resolvers.add(resolver);

    HealthPolicyImpl policy = new HealthPolicyImpl();
    policy.initialize(sensors, detectors, diagnosers, resolvers);

    MetricsSnapshot metrics = policy.executeSensors();
    policy.executeDetectors(metrics);
    policy.executeDiagnosers(metrics, new HashSet<>());
    policy.executeResolver(resolver, metrics, new HashSet<>(), new HashSet<>());

    verify(sensor, times(1)).fetchMetrics();
    verify(detector, times(1)).detect(any());
    verify(diagnoser, times(1)).diagnose(any(), anySet());
    verify(resolver, times(1)).resolve(any(), anySet(), anySet());
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

    policy.executeResolver(null, null, null, null);
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
    policy.executeResolver(null, null, null, null);
    delay = policy.getDelay(TimeUnit.MILLISECONDS);
    Assert.assertEquals(100, delay);
  }

  @Test
  public void testClose() {
    ISensor sensor = mock(ISensor.class);
    IDetector detector = mock(IDetector.class);
    IResolver resolver = mock(IResolver.class);
    IDiagnoser diagnoser = mock(IDiagnoser.class);

    HealthPolicyImpl policy = new HealthPolicyImpl();
    policy.registerSensors(sensor);
    policy.registerDetectors(detector);
    policy.registerDiagnosers(diagnoser);
    policy.registerResolvers(resolver);

    policy.close();

    verify(sensor, times(1)).close();
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
