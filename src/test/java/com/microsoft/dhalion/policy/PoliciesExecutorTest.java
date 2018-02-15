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
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.WantedButNotInvoked;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

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
      verify(mockPolicy, timeout(50l).atLeastOnce()).executeResolvers(diagnosis);
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
}
