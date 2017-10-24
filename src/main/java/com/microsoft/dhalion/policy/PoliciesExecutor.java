/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.policy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.microsoft.dhalion.api.IHealthPolicy;
import com.microsoft.dhalion.api.IResolver;
import com.microsoft.dhalion.detector.Symptom;
import com.microsoft.dhalion.diagnoser.Diagnosis;
import com.microsoft.dhalion.resolver.Action;
import com.microsoft.dhalion.state.MetricsState;

public class PoliciesExecutor {
  private static final Logger LOG = Logger.getLogger(PoliciesExecutor.class.getName());
  private final List<IHealthPolicy> policies;
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private List<Action> actions = new ArrayList<>();
  private MetricsState previousState = null;

  public PoliciesExecutor(List<IHealthPolicy> policies) {
    this.policies = policies;
  }

  public ScheduledFuture<?> start() {
    MetricsState metricsState = new MetricsState();

    ScheduledFuture<?> future = executor.scheduleWithFixedDelay(() -> {
      // schedule the next execution cycle
      Long nextScheduleDelay = policies.stream()
          .map(x -> x.getDelay(TimeUnit.MILLISECONDS)).min(Comparator.naturalOrder()).orElse(10000l);
      if (nextScheduleDelay > 0) {
        try {
          LOG.info("Sleep (millis) before next policy execution cycle: " + nextScheduleDelay);
          TimeUnit.MILLISECONDS.sleep(nextScheduleDelay);
        } catch (InterruptedException e) {
          LOG.warning("Interrupted while waiting for next policy execution cycle");
        }
      }

      for (IHealthPolicy policy : policies) {
        if (policy.getDelay(TimeUnit.MILLISECONDS) > 0) {
          continue;
        }

        LOG.info("Executing Policy: " + policy.getClass().getSimpleName());
        policy.executeSensors(metricsState);
        if(this.actions.size() > 0) {
          policy.evaluateActions(this.actions, previousState, metricsState);
        }
        previousState = metricsState;
        List<Symptom> symptoms = policy.executeDetectors();
        List<Diagnosis> diagnosis = policy.executeDiagnosers(symptoms);
        IResolver resolver = policy.selectResolver(diagnosis);
        this.actions = policy.executeResolver(resolver, diagnosis);

        // The policy execution is complete. Retain the stats, flush the metrics
        metricsState.clearMetrics();
      }
    }, 1, 1, TimeUnit.MILLISECONDS);

    return future;
  }

  public void destroy() {
    this.executor.shutdownNow();
  }
}
