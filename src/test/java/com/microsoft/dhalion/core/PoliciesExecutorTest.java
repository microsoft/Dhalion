/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.microsoft.dhalion.api.IHealthPolicy;
import com.microsoft.dhalion.api.IResolver;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class PoliciesExecutorTest {
  @Test
  public void verifyPeriodicPolicyInvocation() throws Exception {
    IHealthPolicy mockPolicy1 = mock(IHealthPolicy.class);
    when(mockPolicy1.getInterval()).thenReturn(20l);
    IHealthPolicy mockPolicy2 = mock(IHealthPolicy.class);
    when(mockPolicy2.getInterval()).thenReturn(50l);

    List<IHealthPolicy> policies = new ArrayList<>();
    policies.add(mockPolicy1);
    policies.add(mockPolicy2);
    PoliciesExecutor executor = new PoliciesExecutor(policies);
    executor.start();

    verify(mockPolicy1, timeout(1000l).atLeast(5)).executeResolvers(any());
    verify(mockPolicy2, timeout(1000l).atLeast(2)).executeResolvers(any());
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
    when(mockPolicy.executeResolvers(resolver)).thenReturn(actions);
    when(mockPolicy.getInterval()).thenReturn(20l);

    List<IHealthPolicy> policies = new ArrayList<>();
    policies.add(mockPolicy);
    PoliciesExecutor executor = new PoliciesExecutor(policies);
    executor.start();

    verify(mockPolicy, timeout(50l).atLeastOnce()).executeResolvers(resolver);
    InOrder order = Mockito.inOrder(mockPolicy);
    order.verify(mockPolicy).executeDetectors();
    order.verify(mockPolicy).executeDiagnosers(symptoms);
    order.verify(mockPolicy).selectResolver(diagnosis);
    order.verify(mockPolicy).executeResolvers(resolver);

    executor.destroy();
  }
}
