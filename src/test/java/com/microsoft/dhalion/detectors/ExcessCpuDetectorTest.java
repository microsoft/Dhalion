/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.detectors;

import com.microsoft.dhalion.api.IDetector;
import com.microsoft.dhalion.conf.PolicyConfig;
import com.microsoft.dhalion.core.Measurement;
import com.microsoft.dhalion.core.MeasurementsTable;
import com.microsoft.dhalion.core.Symptom;
import com.microsoft.dhalion.core.SymptomsTable;
import com.microsoft.dhalion.policy.PoliciesExecutor.ExecutionContext;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static com.microsoft.dhalion.detectors.ResourceAvailabilityDetector.DEMAND_METRIC_NAME_KEY;
import static com.microsoft.dhalion.detectors.ResourceAvailabilityDetector.FREE_METRIC_NAME_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExcessCpuDetectorTest {
  private ExecutionContext context;

  private IDetector detector;
  private PolicyConfig policyConf;
  private Collection<Measurement> metrics;
  private Collection<Measurement> metrics2;

  private static final String FREE_METRIC = "free_metric";
  private static final String DEMAND_METRIC = "demand_metric";

  @Before
  public void initialize() {
    Instant currentInstant = Instant.now();
    Instant previousInstant = currentInstant.minus(1, ChronoUnit.MINUTES);
    context = mock(ExecutionContext.class);
    when(context.checkpoint()).thenReturn(currentInstant);
    when(context.previousCheckpoint()).thenReturn(previousInstant);

    HashMap<String, Object> conf = new HashMap<>();
    conf.put(ExcessCpuDetector.class.getSimpleName() + FREE_METRIC_NAME_KEY,
        FREE_METRIC);
    conf.put(ExcessCpuDetector.class.getSimpleName() + DEMAND_METRIC_NAME_KEY,
        DEMAND_METRIC);
    policyConf = new PolicyConfig(null, conf);

    Instant metricInstant = currentInstant.minus(30, ChronoUnit.SECONDS);

    Measurement instance1 = new Measurement("c1", "i1", FREE_METRIC, metricInstant, 10);
    Measurement instance2 = new Measurement("c2", "i2", FREE_METRIC, metricInstant, 91);
    Measurement instance3 = new Measurement("c3", "i3", FREE_METRIC, metricInstant, 50);
    Measurement instance4 = new Measurement("c4", "i4", FREE_METRIC, metricInstant, 60);
    Measurement instance5 = new Measurement("c5", "i1", DEMAND_METRIC, metricInstant, 25);
    Measurement instance6 = new Measurement("c6", "i2", DEMAND_METRIC, metricInstant, 20);
    Measurement instance7 = new Measurement("c7", "i3", DEMAND_METRIC, metricInstant, 29);
    Measurement instance8 = new Measurement("c8", "i4", DEMAND_METRIC, metricInstant, 10);

    metricInstant = previousInstant.minus(30, ChronoUnit.SECONDS);
    Measurement instance9 = new Measurement("c1", "i1", FREE_METRIC, metricInstant, 10);
    Measurement instance10 = new Measurement("c2", "i2", FREE_METRIC, metricInstant, 91);
    Measurement instance11 = new Measurement("c3", "i3", FREE_METRIC, metricInstant, 50);
    Measurement instance12 = new Measurement("c4", "i4", FREE_METRIC, metricInstant, 60);
    Measurement instance13 = new Measurement("c5", "i1", DEMAND_METRIC, metricInstant, 25);
    Measurement instance14 = new Measurement("c6", "i2", DEMAND_METRIC, metricInstant, 20);
    Measurement instance15 = new Measurement("c7", "i3", DEMAND_METRIC, metricInstant, 29);
    Measurement instance16 = new Measurement("c8", "i4", DEMAND_METRIC, metricInstant, 10);

    metrics = new ArrayList<>();
    metrics.add(instance1);
    metrics.add(instance2);
    metrics.add(instance3);
    metrics.add(instance4);
    metrics.add(instance5);
    metrics.add(instance6);
    metrics.add(instance7);
    metrics.add(instance8);

    metrics2 = new ArrayList<>();
    metrics2.add(instance9);
    metrics2.add(instance10);
    metrics2.add(instance11);
    metrics2.add(instance12);
    metrics2.add(instance13);
    metrics2.add(instance14);
    metrics2.add(instance15);
    metrics2.add(instance16);
  }

  @Test
  public void testDetectorWithMetricsInRange() {
    when(context.measurements()).thenReturn(MeasurementsTable.of(metrics));

    detector = new ExcessCpuDetector(policyConf);
    detector.initialize(context);

    Collection<Symptom> symptoms = detector.detect(metrics);
    SymptomsTable symptomsTable = SymptomsTable.of(symptoms);

    assertEquals(2, symptomsTable.size());
    assertTrue(symptomsTable.type(SymptomName.EXCESS_CPU.text())
        .assignment("i2").size() > 0);
    assertTrue(symptomsTable.type(SymptomName.EXCESS_CPU.text())
        .assignment("i4").size() > 0);
  }

  @Test
  public void testDetectorWhenMetricsOutOfRange() {
    when(context.measurements()).thenReturn(MeasurementsTable.of(metrics2));

    detector = new ExcessCpuDetector(policyConf);
    detector.initialize(context);

    Collection<Symptom> symptoms = detector.detect(metrics);
    SymptomsTable symptomsTable = SymptomsTable.of(symptoms);

    assertEquals(0, symptomsTable.size());
  }
}
