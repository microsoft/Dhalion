/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.policy;

import com.microsoft.dhalion.api.IHealthPolicy;
import com.microsoft.dhalion.api.IResolver;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class PoliciesExecutorTest {
  @Test
  public void verifyPeriodicPolicyInvocation() throws Exception {
    HealthPolicyImpl policy1 = spy(new HealthPolicyImpl());
    policy1.setPolicyExecutionInterval(TimeUnit.MILLISECONDS, 20);
    HealthPolicyImpl policy2 = spy(new HealthPolicyImpl());
    policy2.setPolicyExecutionInterval(TimeUnit.MILLISECONDS, 50);

    List<IHealthPolicy> policies = new ArrayList<>();
    policies.add(policy1);
    policies.add(policy2);
    PoliciesExecutor executor = new PoliciesExecutor(policies);
    executor.start();

    verify(policy1, timeout(1000l).atLeast(5)).executeResolver(any(), anyList());
    verify(policy2, timeout(1000l).atLeast(2)).executeResolver(any(), anyList());
    executor.destroy();
  }

  @Test
  public void verifyPolicyExecutionOrder() throws Exception {
    List symptoms = new ArrayList<>();
    List diagnosis = new ArrayList<>();
    IResolver resolver = mock(IResolver.class);
    List actions = new ArrayList<>();

    IHealthPolicy mockPolicy = mock(IHealthPolicy.class);
    when(mockPolicy.executeDetectors()).thenReturn(symptoms);
    when(mockPolicy.executeDiagnosers(symptoms)).thenReturn(diagnosis);
    when(mockPolicy.selectResolver(diagnosis)).thenReturn(resolver);
    when(mockPolicy.executeResolver(resolver, diagnosis)).thenReturn(actions);

    List<IHealthPolicy> policies = new ArrayList<>();
    policies.add(mockPolicy);
    PoliciesExecutor executor = new PoliciesExecutor(policies);
    executor.start();

    verify(mockPolicy, timeout(50l).atLeastOnce()).executeResolver(resolver, diagnosis);
    InOrder order = Mockito.inOrder(mockPolicy);
    order.verify(mockPolicy).executeDetectors();
    order.verify(mockPolicy).executeDiagnosers(symptoms);
    order.verify(mockPolicy).selectResolver(diagnosis);
    order.verify(mockPolicy).executeResolver(resolver, diagnosis);

    executor.destroy();
  }
}
