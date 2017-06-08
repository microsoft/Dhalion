/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.policy;

import com.google.common.annotations.VisibleForTesting;
import com.microsoft.dhalion.api.IDetector;
import com.microsoft.dhalion.api.IDiagnoser;
import com.microsoft.dhalion.api.IHealthPolicy;
import com.microsoft.dhalion.api.IResolver;
import com.microsoft.dhalion.detector.Symptom;
import com.microsoft.dhalion.diagnoser.Diagnosis;
import com.microsoft.dhalion.resolver.Action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class HealthPolicyImpl implements IHealthPolicy {
  protected List<IDetector> detectors = new ArrayList<>();
  protected List<IDiagnoser> diagnosers = new ArrayList<>();
  protected List<IResolver> resolvers = new ArrayList<>();

  protected long intervalMillis = TimeUnit.MINUTES.toMillis(1);
  private long lastExecutionTimeMills = 0;
  private long oneTimeDelayTimestamp = 0;

  @VisibleForTesting
  ClockTimeProvider clock = new ClockTimeProvider();

  @Override
  public void initialize(List<IDetector> detectors, List<IDiagnoser> diagnosers, List<IResolver> resolvers) {
    this.detectors = detectors;
    this.diagnosers = diagnosers;
    this.resolvers = resolvers;
  }

  public void registerDetectors(IDetector... detectors) {
    if (detectors == null) {
      throw new IllegalArgumentException("Null instance cannot be added.");
    }
    Arrays.stream(detectors).forEach(detector -> this.detectors.add(detector));
  }

  public void registerDiagnosers(IDiagnoser... diagnosers) {
    if (diagnosers == null) {
      throw new IllegalArgumentException("Null instance cannot be added.");
    }
    Arrays.stream(diagnosers).forEach(diagnoser -> this.diagnosers.add(diagnoser));
  }

  public void registerResolvers(IResolver... resolvers) {
    if (resolvers == null) {
      throw new IllegalArgumentException("Null instance cannot be added.");
    }
    Arrays.stream(resolvers).forEach(resolver -> this.resolvers.add(resolver));
  }

  /**
   * @param unit  the delay unit
   * @param value the delay after which this policy will be re-executed. For a one-time policy, the value will be 0 or
   *              negative
   */
  public void setPolicyExecutionInterval(TimeUnit unit, long value) {
    value = unit.toMillis(value);
    if (value <= 0) {
      value = Long.MAX_VALUE;
    }
    this.intervalMillis = unit.toMillis(value);
  }

  /**
   * Sets the delay before next execution of this policy. This one time delay value overrides the original policy
   * execution interval, set using {@link HealthPolicyImpl#setPolicyExecutionInterval}. All subsequent policy
   * execution will take place using the original delay value.
   *
   * @param unit  the delay unit
   * @param value the delay value
   */
  public void setOneTimeDelay(TimeUnit unit, long value) {
    oneTimeDelayTimestamp = clock.currentTimeMillis() + unit.toMillis(value);
  }

  @Override
  public List<Symptom> executeDetectors() {
    List<Symptom> symptoms = new ArrayList<>();
    if (detectors == null) {
      return symptoms;
    }

    symptoms = detectors.stream().map(detector -> detector.detect())
        .filter(detectedSymptoms -> detectedSymptoms != null)
        .flatMap(List::stream).collect(Collectors.toList());

    return symptoms;
  }

  @Override
  public List<Diagnosis> executeDiagnosers(List<Symptom> symptoms) {
    List<Diagnosis> diagnosis = new ArrayList<>();
    if (diagnosers == null) {
      return diagnosis;
    }

    diagnosis = diagnosers.stream().map(diagnoser -> diagnoser.diagnose(symptoms))
        .filter(diagnoses -> diagnoses != null)
        .collect(Collectors.toList());

    return diagnosis;
  }

  @Override
  public IResolver selectResolver(List<Diagnosis> diagnosis) {
    if (resolvers == null) {
      return null;
    }

    return resolvers.stream().findFirst().orElse(null);
  }

  @Override
  public List<Action> executeResolver(IResolver resolver, List<Diagnosis> diagnosis) {
    if (oneTimeDelayTimestamp > 0 && oneTimeDelayTimestamp <= clock.currentTimeMillis()) {
      // reset one time delay timestamp
      oneTimeDelayTimestamp = 0;
    }

    List<Action> actions = new ArrayList<>();
    if (resolver != null) {
      actions = resolver.resolve(diagnosis);
    }

    lastExecutionTimeMills = clock.currentTimeMillis();
    return actions;
  }

  @Override
  public long getDelay(TimeUnit unit) {
    long delay;
    if (lastExecutionTimeMills <= 0) {
      // first time execution of the policy will start immediately.
      delay = 0;
    } else if (oneTimeDelayTimestamp > 0) {
      delay = oneTimeDelayTimestamp - clock.currentTimeMillis();
    } else {
      delay = lastExecutionTimeMills + intervalMillis - clock.currentTimeMillis();
    }
    delay = delay < 0 ? 0 : delay;

    return unit.convert(delay, TimeUnit.MILLISECONDS);
  }

  @Override
  public void close() {
    if (detectors != null) {
      detectors.forEach(value -> value.close());
    }
    if (diagnosers != null) {
      diagnosers.forEach(value -> value.close());
    }
    if (resolvers != null) {
      resolvers.forEach(value -> value.close());
    }
  }

  @VisibleForTesting
  static class ClockTimeProvider {
    long currentTimeMillis() {
      return System.currentTimeMillis();
    }
  }
}
