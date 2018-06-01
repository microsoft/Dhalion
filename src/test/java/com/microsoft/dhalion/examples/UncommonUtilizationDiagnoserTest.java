package com.microsoft.dhalion.examples;

import com.microsoft.dhalion.Utils;
import com.microsoft.dhalion.conf.Config;
import com.microsoft.dhalion.conf.PolicyConfig;
import com.microsoft.dhalion.core.Diagnosis;
import com.microsoft.dhalion.core.DiagnosisTable;
import com.microsoft.dhalion.core.Symptom;
import com.microsoft.dhalion.policy.PoliciesExecutor.ExecutionContext;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;

import static com.microsoft.dhalion.detectors.AboveThresholdDetector.SYMPTOM_HIGH;
import static com.microsoft.dhalion.detectors.BelowThresholdDetector.SYMPTOM_LOW;
import static com.microsoft.dhalion.examples.MetricName.METRIC_CPU;
import static com.microsoft.dhalion.examples.MetricName.METRIC_MEMORY;
import static com.microsoft.dhalion.examples.UncommonUtilizationDiagnoser.DIAGNOSIS_OVER_UTILIZED_NODE;
import static com.microsoft.dhalion.examples.UncommonUtilizationDiagnoser.DIAGNOSIS_UNDER_UTILIZED_NODE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UncommonUtilizationDiagnoserTest {

  private ExecutionContext context;
  private Instant currentInstant;
  private UncommonUtilizationDiagnoser diagnoser;
  private PolicyConfig policyConf;
  private Config healthMgrConfig;

  private Collection<String> assignments = Arrays.asList("NodeA[1]", "NodeB[2]");
  private Collection<String> assignments2 = Arrays.asList("NodeA[2]", "NodeB[1]");

  @Before
  public void initialize() {

    currentInstant = Instant.now();
    context = mock(ExecutionContext.class);
    when(context.checkpoint()).thenReturn(currentInstant);

    policyConf = mock(PolicyConfig.class);
    healthMgrConfig = mock(Config.class);
  }

  @Test
  public void diagnosisUncommonUtilization() {
    Symptom symptom1 = new Symptom(Utils.getCompositeName(SYMPTOM_HIGH, METRIC_CPU.text()), currentInstant,
                                   assignments);
    Symptom symptom2 = new Symptom(Utils.getCompositeName(SYMPTOM_LOW, METRIC_CPU.text()), currentInstant,
                                   assignments2);
    Symptom symptom3 = new Symptom(Utils.getCompositeName(SYMPTOM_HIGH, METRIC_MEMORY.text()), currentInstant, assignments);
    Symptom symptom4 = new Symptom(Utils.getCompositeName(SYMPTOM_LOW, METRIC_MEMORY.text()), currentInstant, assignments2);
    Collection<Symptom> symptoms = Arrays.asList(symptom1, symptom2, symptom3, symptom4);

    diagnoser = new UncommonUtilizationDiagnoser(policyConf, healthMgrConfig);
    diagnoser.initialize(context);

    Collection<Diagnosis> diagnoses = diagnoser.diagnose(symptoms);
    assertEquals(2, diagnoses.size());
    DiagnosisTable diagnosisTable = DiagnosisTable.of(diagnoses);
    assertEquals(2, diagnosisTable.type(DIAGNOSIS_OVER_UTILIZED_NODE).size());
    assertEquals(2, diagnosisTable.type(DIAGNOSIS_UNDER_UTILIZED_NODE).size());
    assertEquals(1, diagnosisTable.type(DIAGNOSIS_OVER_UTILIZED_NODE).assignment("NodeA[1]").size());
    assertEquals(1, diagnosisTable.type(DIAGNOSIS_OVER_UTILIZED_NODE).assignment("NodeB[2]").size());
    assertEquals(1, diagnosisTable.type(DIAGNOSIS_UNDER_UTILIZED_NODE).assignment("NodeA[2]").size());
    assertEquals(1, diagnosisTable.type(DIAGNOSIS_UNDER_UTILIZED_NODE).assignment("NodeB[1]").size());
  }

  @Test
  public void diagnosisUncommonUtilization2() {
    Symptom symptom1 = new Symptom(Utils.getCompositeName(SYMPTOM_HIGH, METRIC_CPU.text()), currentInstant, assignments);
    Symptom symptom2 = new Symptom(Utils.getCompositeName(SYMPTOM_LOW, METRIC_CPU.text()), currentInstant, assignments2);
    Symptom symptom3 = new Symptom(Utils.getCompositeName(SYMPTOM_HIGH, METRIC_MEMORY.text()), currentInstant, assignments2);
    Symptom symptom4 = new Symptom(Utils.getCompositeName(SYMPTOM_LOW, METRIC_MEMORY.text()), currentInstant, assignments);
    Collection<Symptom> symptoms = Arrays.asList(symptom1, symptom2, symptom3, symptom4);

    diagnoser = new UncommonUtilizationDiagnoser(policyConf, healthMgrConfig);
    diagnoser.initialize(context);

    Collection<Diagnosis> diagnoses = diagnoser.diagnose(symptoms);
    assertEquals(0, diagnoses.size());
  }

  @Test
  public void diagnosisUncommonUtilization3() {
    Symptom symptom1 = new Symptom(Utils.getCompositeName(SYMPTOM_HIGH, METRIC_CPU.text()), currentInstant, assignments);
    Symptom symptom2 = new Symptom(Utils.getCompositeName(SYMPTOM_HIGH, METRIC_MEMORY.text()), currentInstant, assignments);
    Collection<Symptom> symptoms = Arrays.asList(symptom1, symptom2);

    diagnoser = new UncommonUtilizationDiagnoser(policyConf, healthMgrConfig);
    diagnoser.initialize(context);

    Collection<Diagnosis> diagnoses = diagnoser.diagnose(symptoms);
    assertEquals(1, diagnoses.size());
    DiagnosisTable diagnosisTable = DiagnosisTable.of(diagnoses);
    assertEquals(2, diagnosisTable.type(DIAGNOSIS_OVER_UTILIZED_NODE).size());
    assertEquals(1, diagnosisTable.type(DIAGNOSIS_OVER_UTILIZED_NODE).assignment("NodeA[1]").size());
    assertEquals(1, diagnosisTable.type(DIAGNOSIS_OVER_UTILIZED_NODE).assignment("NodeB[2]").size());
  }
}
