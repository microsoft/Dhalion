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
import com.microsoft.dhalion.api.ISensor;
import com.microsoft.dhalion.detector.Symptom;
import com.microsoft.dhalion.diagnoser.Diagnosis;
import com.microsoft.dhalion.resolver.Action;
import com.microsoft.dhalion.state.MetricsSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class HealthPolicyImpl implements IHealthPolicy {
  protected Set<ISensor> sensors = new HashSet<>();
  protected Set<IDetector> detectors = new HashSet<>();
  protected Set<IDiagnoser> diagnosers = new HashSet<>();
  protected Set<IResolver> resolvers = new HashSet<>();

  protected long intervalMillis = TimeUnit.MINUTES.toMillis(1);
  @VisibleForTesting
  ClockTimeProvider clock = new ClockTimeProvider();
  private long lastExecutionTimeMills = 0;
  private long oneTimeDelayTimestamp = 0;

  @Override
  public void initialize(Set<ISensor> sensors, Set<IDetector> detectors, Set<IDiagnoser> diagnosers,
                         Set<IResolver> resolvers) {
    this.sensors = sensors;
    this.detectors = detectors;
    this.diagnosers = diagnosers;
    this.resolvers = resolvers;
  }

  public void registerSensors(ISensor... sensors) {
    if (sensors == null) {
      throw new IllegalArgumentException("Null instance cannot be added.");
    }
    Arrays.stream(sensors).forEach(sensor -> this.sensors.add(sensor));
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
  public MetricsSnapshot executeSensors() {
    MetricsSnapshot metrics = new MetricsSnapshot();
    if (sensors == null) {
      return null;
    }
    sensors.stream().forEach(sensor -> metrics.addMetrics(sensor.fetchMetrics()));
    return metrics;
  }

  @Override
  public Set<Symptom> executeDetectors(MetricsSnapshot metrics) {
    Set<Symptom> symptoms = new HashSet<>();
    if (detectors == null) {
      return symptoms;
    }

    symptoms = detectors.stream().map(detector -> detector.detect(metrics))
        .filter(detectedSymptoms -> detectedSymptoms != null)
        .flatMap(Set::stream).collect(Collectors.toSet());

    return symptoms;
  }

  @Override
  public Set<Diagnosis> executeDiagnosers(MetricsSnapshot metrics, Set<Symptom> symptoms) {
    Set<Diagnosis> diagnosis = new HashSet<>();
    if (diagnosers == null) {
      return diagnosis;
    }

    diagnosis = diagnosers.stream().map(diagnoser -> diagnoser.diagnose(metrics, symptoms))
        .filter(diagnoses -> diagnoses != null).flatMap(x -> x.stream())
        .collect(Collectors.toSet());

    return diagnosis;
  }

  @Override
  public IResolver selectResolver(MetricsSnapshot metrics, Set<Symptom> symptoms, Set<Diagnosis> diagnosis) {
    if (resolvers == null) {
      return null;
    }

    return resolvers.stream().findFirst().orElse(null);
  }

  @Override
  public List<Action> executeResolver(IResolver resolver, MetricsSnapshot metrics, Set<Symptom> symptoms,
                                      Set<Diagnosis> diagnosis) {
    if (oneTimeDelayTimestamp > 0 && oneTimeDelayTimestamp <= clock.currentTimeMillis()) {
      // reset one time delay timestamp
      oneTimeDelayTimestamp = 0;
    }

    List<Action> actions = new ArrayList<>();
    if (resolver != null) {
      actions = resolver.resolve(metrics, symptoms, diagnosis);
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
    if (sensors != null) {
      sensors.forEach(value -> value.close());
    }
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

  @Override
  public Set<IDetector> getDetectors() {
    return this.detectors;
  }

  @Override
  public Set<IDiagnoser> getDiagnosers() {
    return this.diagnosers;
  }

  @Override
  public Set<IResolver> getResolvers() {
    return this.resolvers;
  }

  @VisibleForTesting
  static class ClockTimeProvider {
    long currentTimeMillis() {
      return System.currentTimeMillis();
    }
  }
}
