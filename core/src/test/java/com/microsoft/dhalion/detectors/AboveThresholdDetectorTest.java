package com.microsoft.dhalion.detectors;

import com.microsoft.dhalion.Utils;
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

import static com.microsoft.dhalion.detectors.AboveThresholdDetector.ABOVE_THRESHOLD_NO_CHECKPOINTS;
import static com.microsoft.dhalion.detectors.AboveThresholdDetector.HIGH_THRESHOLD_CONF;
import static com.microsoft.dhalion.detectors.AboveThresholdDetector.SYMPTOM_HIGH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AboveThresholdDetectorTest {
  private ExecutionContext context;
  private Instant currentInstant;
  private Instant previousInstant;

  private IDetector detector;
  private PolicyConfig policyConf;
  private Collection<Measurement> metrics;
  private Collection<Measurement> metrics2;

  @Before
  public void initialize() {
    currentInstant = Instant.now();
    previousInstant = currentInstant.minus(1, ChronoUnit.MINUTES);
    context = mock(ExecutionContext.class);
    when(context.checkpoint()).thenReturn(currentInstant);

    Measurement instance1 = new Measurement("c1", "i1", "cpu", currentInstant, 10);
    Measurement instance2 = new Measurement("c2", "i2", "cpu", currentInstant, 91);
    Measurement instance3 = new Measurement("c3", "i3", "cpu", currentInstant, 50);
    Measurement instance4 = new Measurement("c4", "i4", "cpu", currentInstant, 60);
    Measurement instance5 = new Measurement("c5", "i5", "cpu", currentInstant, 95);
    Measurement instance6 = new Measurement("c6", "i6", "cpu", currentInstant, 80);
    Measurement instance7 = new Measurement("c7", "i7", "cpu", currentInstant, 9);
    Measurement instance8 = new Measurement("c8", "i8", "cpu", currentInstant, 60);

    Measurement instance9 = new Measurement("c1", "i1", "cpu", previousInstant, 10);
    Measurement instance10 = new Measurement("c2", "i2", "cpu", previousInstant, 9);
    Measurement instance11 = new Measurement("c3", "i3", "cpu", previousInstant, 50);
    Measurement instance12 = new Measurement("c4", "i4", "cpu", previousInstant, 60);
    Measurement instance13 = new Measurement("c5", "i5", "cpu", previousInstant, 95);
    Measurement instance14 = new Measurement("c6", "i6", "cpu", previousInstant, 80);
    Measurement instance15 = new Measurement("c7", "i7", "cpu", previousInstant, 9);
    Measurement instance16 = new Measurement("c8", "i8", "cpu", previousInstant, 60);

    metrics = new ArrayList<>();
    metrics.add(instance1);
    metrics.add(instance2);
    metrics.add(instance3);
    metrics.add(instance4);
    metrics.add(instance5);
    metrics.add(instance6);
    metrics.add(instance7);
    metrics.add(instance8);

    metrics2 = new ArrayList<>(metrics);
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
  public void testThresholdBasedDetector() {
    when(context.measurements()).thenReturn(MeasurementsTable.of(metrics));
    HashMap<String, Object> conf = new HashMap();
    conf.put(Utils.getCompositeName(HIGH_THRESHOLD_CONF, "cpu"), 90.0);
    conf.put(Utils.getCompositeName(ABOVE_THRESHOLD_NO_CHECKPOINTS, "cpu"), 1.0);
    policyConf = new PolicyConfig(null, conf);

    detector = new AboveThresholdDetector(policyConf, "cpu");
    detector.initialize(context);

    Collection<Symptom> symptoms = detector.detect(metrics);
    SymptomsTable symptomsTable = SymptomsTable.of(symptoms);

    assertEquals(2, symptomsTable.size());
    assertTrue(symptomsTable.type(Utils.getCompositeName(
        SYMPTOM_HIGH, "cpu")).assignment("i2").size() > 0);
    assertTrue(symptomsTable.type(Utils.getCompositeName(
        SYMPTOM_HIGH, "cpu")).assignment("i5").size() > 0);
  }

  @Test
  public void testThresholdBasedDetector2() {
    when(context.measurements()).thenReturn(MeasurementsTable.of(metrics2));
    HashMap<String, Object> conf = new HashMap();
    conf.put(Utils.getCompositeName(HIGH_THRESHOLD_CONF, "cpu"), 90.0);
    conf.put(Utils.getCompositeName(ABOVE_THRESHOLD_NO_CHECKPOINTS, "cpu"), 2.0);
    policyConf = new PolicyConfig(null, conf);

    detector = new AboveThresholdDetector(policyConf, "cpu");
    detector.initialize(context);

    Collection<Symptom> symptoms = detector.detect(metrics);
    SymptomsTable symptomsTable = SymptomsTable.of(symptoms);

    assertEquals(1, symptomsTable.size());
    assertTrue(symptomsTable.type(Utils.getCompositeName(
        SYMPTOM_HIGH, "cpu")).assignment("i5").size() > 0);
  }
}
