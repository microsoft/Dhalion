/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.policy;

import com.microsoft.dhalion.api.IHealthPolicy;
import com.microsoft.dhalion.core.Action;
import com.microsoft.dhalion.core.Diagnosis;
import com.microsoft.dhalion.core.Measurement;
import com.microsoft.dhalion.core.Symptom;
import com.microsoft.dhalion.policy.PoliciesExecutor.ExecutionContext;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.WantedButNotInvoked;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PoliciesExecutorTest {
  @Test
  public void verifyPeriodicPolicyInvocation() throws Exception {
    HealthPolicyImpl policy1 = spy(new HealthPolicyImpl());
    policy1.setPolicyExecutionInterval(Duration.ofMillis(20));
    HealthPolicyImpl policy2 = spy(new HealthPolicyImpl());
    policy2.setPolicyExecutionInterval(Duration.ofMillis(50));

    List<IHealthPolicy> policies = Arrays.asList(policy1, policy2);
    PoliciesExecutor executor = new PoliciesExecutor(policies);
    executor.start();

    verify(policy1, timeout(1000l).atLeast(5)).executeResolvers(anyList());
    verify(policy2, timeout(1000l).atLeast(2)).executeResolvers(anyList());
    executor.destroy();
  }

  @Test
  public void verifyPolicyExecutionOrder() throws Exception {
    List<Measurement> measurements = new ArrayList<>();
    List<Symptom> symptoms = new ArrayList<>();
    List<Diagnosis> diagnosis = new ArrayList<>();
    List<Action> actions = new ArrayList<>();

    IHealthPolicy mockPolicy = mock(IHealthPolicy.class);
    when(mockPolicy.getDelay()).thenReturn(Duration.ZERO);
    when(mockPolicy.executeSensors()).thenReturn(measurements);
    when(mockPolicy.executeDetectors(measurements)).thenReturn(symptoms);
    when(mockPolicy.executeDiagnosers(symptoms)).thenReturn(diagnosis);
    when(mockPolicy.executeResolvers(diagnosis)).thenReturn(actions);

    List<IHealthPolicy> policies = Collections.singletonList(mockPolicy);
    PoliciesExecutor executor = new PoliciesExecutor(policies);
    ScheduledFuture<?> future = executor.start();

    try {
      verify(mockPolicy, timeout(200l).atLeastOnce()).executeResolvers(diagnosis);
    } catch (WantedButNotInvoked e) {
      if (future.isDone()) {
        System.out.println(future.get());
      }
      throw e;
    }


    InOrder order = Mockito.inOrder(mockPolicy);
    order.verify(mockPolicy).executeSensors();
    order.verify(mockPolicy).executeDetectors(measurements);
    order.verify(mockPolicy).executeDiagnosers(symptoms);
    order.verify(mockPolicy).executeResolvers(diagnosis);

    executor.destroy();
  }

  @Test
  public void verifyExpiry() throws Exception {
    Instant now = Instant.now();
    Instant old = now.minus(Duration.ofMinutes(35));

    Measurement mRetain = new Measurement("c", "i", "m", now, 123);
    Measurement mExpire = new Measurement("c", "i", "m", old, 123);
    List<Measurement> measurements = Arrays.asList(mRetain, mExpire);

    Symptom sRetain = new Symptom("s", now, null, null);
    Symptom sExpire = new Symptom("s", old, null, null);
    List<Symptom> symptoms = Arrays.asList(sRetain, sExpire);

    Diagnosis dRetain = new Diagnosis("d", now, null, null);
    Diagnosis dExpire = new Diagnosis("d", old, null, null);
    List<Diagnosis> diagnosis = Arrays.asList(dRetain, dExpire);

    Action aRetain = new Action("a", now, null, null);
    Action aExpire = new Action("a", old, null, null);
    List<Action> actions = Arrays.asList(aRetain, aExpire);

    CountDownLatch barrier = new CountDownLatch(1);
    final AtomicInteger executeCount = new AtomicInteger();
    HealthPolicyImpl barrierPolicy = new HealthPolicyImpl() {
      private ExecutionContext context;

      @Override
      public void initialize(ExecutionContext ctxt) {
        this.context = ctxt;
      }

      @Override
      public Duration getDelay() {
        if (executeCount.get() == 1 && barrier.getCount() > 0) {
          // verify result of expiry in previous cycle
          assertEquals(2, measurements.size());
          assertEquals(1, context.measurements().size());
          assertEquals(now, context.measurements().get().iterator().next().instant());

          assertEquals(2, symptoms.size());
          assertEquals(1, context.symptoms().size());
          assertEquals(now, context.symptoms().get().iterator().next().instant());

          assertEquals(2, diagnosis.size());
          assertEquals(1, context.diagnosis().size());
          assertEquals(now, context.diagnosis().get().iterator().next().instant());

          assertEquals(2, actions.size());
          assertEquals(1, context.actions().size());
          assertEquals(now, context.actions().get().iterator().next().instant());

          barrier.countDown();
        }
        return Duration.ZERO;
      }

      @Override
      public Collection<Measurement> executeSensors() {
        executeCount.incrementAndGet();
        return measurements;
      }

      @Override
      public Collection<Symptom> executeDetectors(Collection<Measurement> measurements) {
        return symptoms;
      }

      @Override
      public Collection<Diagnosis> executeDiagnosers(Collection<Symptom> symptoms) {
        return diagnosis;
      }

      @Override
      public Collection<Action> executeResolvers(Collection<Diagnosis> diagnosis) {
        return actions;
      }
    };

    List<IHealthPolicy> policies = Collections.singletonList(barrierPolicy);
    PoliciesExecutor executor = new PoliciesExecutor(policies);
    ScheduledFuture<?> future = executor.start();
    barrier.await(500, TimeUnit.MILLISECONDS);
    if (future.isDone()) {
      future.get();
    }
    executor.destroy();
  }
}
