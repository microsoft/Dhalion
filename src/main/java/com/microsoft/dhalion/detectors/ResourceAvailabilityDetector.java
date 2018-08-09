/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.detectors;

import com.microsoft.dhalion.core.Measurement;
import com.microsoft.dhalion.core.MeasurementsTable;
import com.microsoft.dhalion.core.Symptom;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link ResourceAvailabilityDetector} detects resource availability issues, for e.g. insufficient available
 * memory to service pending jobs. The detector compares amount of free resources ({@code freeMetric}) and the amount
 * of resources needed ({@code demandMetric}).
 * <p></p>
 * The user needs to provide names/types of the {@link Measurement}s representing the freeMetric and the
 * demandMetric, duration over which the resource availability should be evaluated, and the {@link Symptom} type/name
 * to be generated if an issue is observed.
 * <p></p>
 * The detector evaluates identifies all unique instances and requests resource availability evaluation for each of
 * the instances.
 */
public abstract class ResourceAvailabilityDetector extends Detector {
  private static final Logger LOG = Logger.getLogger(ResourceAvailabilityDetector.class.getName());

  public static final String FREE_METRIC_NAME_KEY = ".free.metric.name";
  public static final String DEMAND_METRIC_NAME_KEY = ".demand.metric.name";
  public static final String DURATION_KEY = ".duration";

  private final String freeMetric;
  private final String demandMetric;
  private final Duration duration;
  private final String symptomType;

  public ResourceAvailabilityDetector(String freeMetric, String demandMetric, Duration duration, String symptomType) {
    this.freeMetric = freeMetric;
    this.demandMetric = demandMetric;
    this.duration = duration;
    this.symptomType = symptomType;
  }

  @Override
  public Collection<Symptom> detect(Collection<Measurement> measurements) {
    Instant now = context.checkpoint();

    MeasurementsTable filteredMeasurements = context.measurements().between(now.minus(duration), now);
    if (filteredMeasurements.size() == 0) {
      LOG.fine("Did not find any measurements to evaluate resource availability");
      return Collections.emptyList();
    }

    Collection<String> assignments = new ArrayList<>();
    Collection<String> instances = filteredMeasurements.uniqueInstances();
    for (String instance : instances) {
      double totalFree = aggregate(filteredMeasurements.instance(instance).type(freeMetric));
      double totalDemand = aggregate(filteredMeasurements.instance(instance).type(demandMetric));
      if (evaluate(instance, totalFree, totalDemand)) {
        assignments.add(instance);
      }
    }

    Symptom symptom = new Symptom(symptomType, now, assignments);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine(String.format("Symptom (%s) created for %s", symptom, toString()));
    }
    return Collections.singletonList(symptom);
  }

  protected double aggregate(MeasurementsTable metrics) {
    return metrics.mean();
  }

  abstract protected boolean evaluate(String instance, double free, double used);

  @Override
  public String toString() {
    return "ResourceAvailabilityDetector{" +
        "freeMetric='" + freeMetric + '\'' +
        ", demandMetric='" + demandMetric + '\'' +
        ", duration=" + duration +
        ", symptomType='" + symptomType + '\'' +
        "} ";
  }
}
