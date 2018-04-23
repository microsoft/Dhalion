/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.policy;

import com.microsoft.dhalion.api.IHealthPolicy;
import com.microsoft.dhalion.core.Action;
import com.microsoft.dhalion.core.ActionTable;
import com.microsoft.dhalion.core.Diagnosis;
import com.microsoft.dhalion.core.DiagnosisTable;
import com.microsoft.dhalion.core.Measurement;
import com.microsoft.dhalion.core.MeasurementsTable;
import com.microsoft.dhalion.core.Outcome;
import com.microsoft.dhalion.core.Symptom;
import com.microsoft.dhalion.core.SymptomsTable;

import java.time.Duration;
import java.time.Instant;
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
    for (IHealthPolicy policy : this.policies) {
      ExecutionContext ctx = new ExecutionContext(policy);
      policy.initialize(ctx);
      policyContextMap.put(policy, ctx);
    }
  }

  public ScheduledFuture<?> start() {
    ScheduledFuture<?> future = executor.scheduleWithFixedDelay(() -> {
      // schedule the next execution cycle
      Duration nextScheduleDelay = policies.stream()
          .map(IHealthPolicy::getDelay)
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

        ExecutionContext context = policyContextMap.get(policy);
        context.captureCheckpoint();
        Instant previous = context.previousCheckpoint;
        Instant current = context.checkpoint;

        LOG.info(String.format("Executing Policy: %s, checkpoint: %s",
                               policy.getClass().getSimpleName(),
                               context.checkpoint));

        Collection<Measurement> measurements = policy.executeSensors();
        measurements.stream()
            .filter(m -> m.instant().isAfter(current) || m.instant().isBefore(previous))
            .forEach(m -> LOG.info(m.toString() + "is outside checkpoint window"));
        context.measurementsTableBuilder.addAll(measurements);

        Collection<Symptom> symptoms = policy.executeDetectors(measurements);
        identifyOutliers(previous, current, symptoms);
        context.symptomsTableBuilder.addAll(symptoms);

        Collection<Diagnosis> diagnosis = policy.executeDiagnosers(symptoms);
        identifyOutliers(previous, current, diagnosis);
        context.diagnosisTableBuilder.addAll(diagnosis);

        Collection<Action> actions = policy.executeResolvers(diagnosis);
        identifyOutliers(previous, current, actions);
        context.actionTableBuilder.addAll(actions);

        // TODO pretty print
        LOG.info(actions.toString());

        Instant expiration = current.minus(Duration.ofMinutes(30));
        context.measurementsTableBuilder.expireBefore(expiration);
        context.symptomsTableBuilder.expireBefore(expiration);
        context.diagnosisTableBuilder.expireBefore(expiration);
        context.actionTableBuilder.expireBefore(expiration);
      }
    }, 1, 1, TimeUnit.MILLISECONDS);

    return future;
  }

  private void identifyOutliers(Instant previous, Instant current, Collection<? extends Outcome> outcomes) {
    outcomes.stream()
        .filter(m -> m.instant().isAfter(current) || m.instant().isBefore(previous))
        .forEach(m -> LOG.warning(m.toString() + " is outside checkpoint window"));
  }

  public void destroy() {
    this.executor.shutdownNow();
  }


  public static class ExecutionContext {
    private final MeasurementsTable.Builder measurementsTableBuilder;
    private final SymptomsTable.Builder symptomsTableBuilder;
    private final DiagnosisTable.Builder diagnosisTableBuilder;
    private final ActionTable.Builder actionTableBuilder;
    private Instant checkpoint;
    private Instant previousCheckpoint;
    private IHealthPolicy policy;

    private ExecutionContext(IHealthPolicy policy) {
      this.policy = policy;
      measurementsTableBuilder = new MeasurementsTable.Builder();
      symptomsTableBuilder = new SymptomsTable.Builder();
      diagnosisTableBuilder = new DiagnosisTable.Builder();
      actionTableBuilder = new ActionTable.Builder();
    }

    private void captureCheckpoint() {
      previousCheckpoint = checkpoint != null ? checkpoint : Instant.MIN;
      checkpoint = policy.getNextCheckpoint();
    }

    public MeasurementsTable measurements() {
      return measurementsTableBuilder.get();
    }

    public SymptomsTable symptoms() {
      return symptomsTableBuilder.get();
    }

    public DiagnosisTable diagnosis() {
      return diagnosisTableBuilder.get();
    }

    public ActionTable actions() {
      return actionTableBuilder.get();
    }

    /**
     * A checkpoint is a timestamp at which policy execution begins. This value can be used to identify outcomes
     * created during a particular cycle.
     *
     * @return the timestamp(checkpoint) at which this policy's current execution started
     */
    public Instant checkpoint() {
      return checkpoint;
    }

    /**
     * @return the timestamp(checkpoint) at which this policy's previous execution had started
     */
    public Instant previousCheckpoint() {
      return previousCheckpoint;
    }
  }
}
