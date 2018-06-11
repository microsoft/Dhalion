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

import static com.microsoft.dhalion.detectors.BelowThresholdDetector.BELOW_THRESHOLD_NO_CHECKPOINTS;
import static com.microsoft.dhalion.detectors.BelowThresholdDetector.LOW_THRESHOLD_CONF;
import static com.microsoft.dhalion.detectors.BelowThresholdDetector.SYMPTOM_LOW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BelowThresholdDetectorTest {
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

    Measurement instance1 = new Measurement("c1", "i1", "memory", currentInstant, 10);
    Measurement instance2 = new Measurement("c2", "i2", "memory", currentInstant, 91);
    Measurement instance3 = new Measurement("c3", "i3", "memory", currentInstant, 50);
    Measurement instance4 = new Measurement("c4", "i4", "memory", currentInstant, 60);
    Measurement instance5 = new Measurement("c5", "i5", "memory", currentInstant, 20);
    Measurement instance6 = new Measurement("c6", "i6", "memory", currentInstant, 80);
    Measurement instance7 = new Measurement("c7", "i7", "memory", currentInstant, 9);
    Measurement instance8 = new Measurement("c8", "i8", "memory", currentInstant, 60);

    Measurement instance9 = new Measurement("c1", "i1", "memory", previousInstant, 90);
    Measurement instance10 = new Measurement("c2", "i2", "memory", previousInstant, 9);
    Measurement instance11 = new Measurement("c3", "i3", "memory", previousInstant, 50);
    Measurement instance12 = new Measurement("c4", "i4", "memory", previousInstant, 60);
    Measurement instance13 = new Measurement("c5", "i5", "memory", previousInstant, 20);
    Measurement instance14 = new Measurement("c6", "i6", "memory", previousInstant, 80);
    Measurement instance15 = new Measurement("c7", "i7", "memory", previousInstant, 11);
    Measurement instance16 = new Measurement("c8", "i8", "memory", previousInstant, 60);

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
  public void testThresholdBasedDetectorCurrentCheckpoint() {

    when(context.measurements()).thenReturn(MeasurementsTable.of(metrics));
    HashMap<String, Object> conf = new HashMap();
    conf.put(Utils.getCompositeName(LOW_THRESHOLD_CONF, "memory"), 15.0);
    conf.put(Utils.getCompositeName(BELOW_THRESHOLD_NO_CHECKPOINTS, "memory"), 1.0);
    policyConf = new PolicyConfig(null, conf);

    detector = new BelowThresholdDetector(policyConf, "memory");
    detector.initialize(context);

    Collection<Symptom> symptoms = detector.detect(metrics);
    SymptomsTable symptomsTable = SymptomsTable.of(symptoms);

    assertEquals(2, symptomsTable.size());
    assertTrue(symptomsTable.type(Utils.getCompositeName(SYMPTOM_LOW ,"memory")).assignment("i1").size() > 0);
    assertTrue(symptomsTable.type(Utils.getCompositeName(SYMPTOM_LOW ,"memory")).assignment("i7").size() > 0);

  }

  @Test
  public void testThresholdBasedDetectorTwoCheckpoints() {

    when(context.measurements()).thenReturn(MeasurementsTable.of(metrics2));
    HashMap<String, Object> conf = new HashMap();
    conf.put(Utils.getCompositeName(LOW_THRESHOLD_CONF, "memory"), 15.0);
    conf.put(Utils.getCompositeName(BELOW_THRESHOLD_NO_CHECKPOINTS, "memory"), 2.0);
    policyConf = new PolicyConfig(null, conf);

    detector = new BelowThresholdDetector(policyConf, "memory");
    detector.initialize(context);

    Collection<Symptom> symptoms = detector.detect(metrics);
    SymptomsTable symptomsTable = SymptomsTable.of(symptoms);
    assertEquals(1, symptomsTable.size());
    assertTrue(symptomsTable.type(Utils.getCompositeName(SYMPTOM_LOW ,"memory")).assignment("i7").size() > 0);

  }

}
