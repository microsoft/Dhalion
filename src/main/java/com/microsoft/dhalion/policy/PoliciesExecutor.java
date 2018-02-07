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
import com.microsoft.dhalion.core.MeasurementsArray;
import com.microsoft.dhalion.core.Symptom;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PoliciesExecutor {
  private static final Logger LOG = Logger.getLogger(PoliciesExecutor.class.getName());
  private final List<IHealthPolicy> policies;
  private final Map<IHealthPolicy, ExecutionContext> policyContextMap = new HashMap<>();
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

  public PoliciesExecutor(Collection<IHealthPolicy> policies) {
    this.policies = new ArrayList<>(policies);
    for (IHealthPolicy policy : policies) {
      ExecutionContext ctx = new ExecutionContext();
      policy.initialize(ctx);
      policyContextMap.put(policy, ctx);
    }
  }

  public ScheduledFuture<?> start() {
    ScheduledFuture<?> future = executor.scheduleWithFixedDelay(() -> {
      // schedule the next execution cycle
      Duration nextScheduleDelay = policies.stream()
          .map(x -> x.getDelay())
          .min(Comparator.naturalOrder())
          .orElse(Duration.ofSeconds(10));

      if (nextScheduleDelay.toMillis() > 0) {
        try {
          LOG.info("Sleep (millis) before next policy execution cycle: " + nextScheduleDelay);
          TimeUnit.MILLISECONDS.sleep(nextScheduleDelay.toMillis());
        } catch (InterruptedException e) {
          LOG.warning("Interrupted while waiting for next policy execution cycle");
        }
      }

      for (IHealthPolicy policy : policies) {
        if (policy.getDelay().toMillis() > 0) {
          continue;
        }

        LOG.info("Executing Policy: " + policy.getClass().getSimpleName());
        Collection<Measurement> measurements = policy.executeSensors();
        policyContextMap.get(policy).measurementsArrayBuilder.addAll(measurements);

        Collection<Symptom> symptoms = policy.executeDetectors(measurements);
        Collection<Diagnosis> diagnosis = policy.executeDiagnosers(symptoms);
        Collection<Action> actions = policy.executeResolvers(diagnosis);
        // TODO pretty print
        LOG.info(actions.toString());

        // TODO delete expired objects from state store
      }

    }, 1, 1, TimeUnit.MILLISECONDS);

    return future;
  }

  public void destroy() {
    this.executor.shutdownNow();
  }


  public static class ExecutionContext {
    private final MeasurementsArray.Builder measurementsArrayBuilder;

    public ExecutionContext() {
      measurementsArrayBuilder = new MeasurementsArray.Builder();
    }

    public MeasurementsArray measurements() {
      return measurementsArrayBuilder.get();
    }
  }
}
