package com.microsoft.dhalion.examples;

import com.microsoft.dhalion.Utils;
import com.microsoft.dhalion.api.IDiagnoser;
import com.microsoft.dhalion.conf.Config;
import com.microsoft.dhalion.conf.PolicyConfig;
import com.microsoft.dhalion.core.Diagnosis;
import com.microsoft.dhalion.core.Symptom;
import com.microsoft.dhalion.core.SymptomsTable;
import com.microsoft.dhalion.policy.PoliciesExecutor.ExecutionContext;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import static com.microsoft.dhalion.detectors.AboveThresholdDetector.SYMPTOM_HIGH;
import static com.microsoft.dhalion.detectors.BelowThresholdDetector.SYMPTOM_LOW;
import static com.microsoft.dhalion.examples.MetricName.METRIC_CPU;
import static com.microsoft.dhalion.examples.MetricName.METRIC_MEMORY;

/**
 * This is an example Diagnoser that is used by the Alert Policy. It operates on the example data file
 * and returns DIAGNOSIS_OVER_UTILIZED_NODE diagnosis when a node in the data file has both high cpu and memory
 * utilization. It returns a DIAGNOSIS_UNDER_UTILIZED_NODE diagnosis when a node has both low cpu and memory
 * utilization.
 */
public class UncommonUtilizationDiagnoser implements IDiagnoser {

  private static final Logger LOG = Logger.getLogger(UncommonUtilizationDiagnoser.class.getSimpleName());
  public static String DIAGNOSIS_OVER_UTILIZED_NODE = Utils.getCompositeName(UncommonUtilizationDiagnoser.class
                                                                                 .getSimpleName(), "over_utilized_node");
  public static String DIAGNOSIS_UNDER_UTILIZED_NODE = Utils.getCompositeName(UncommonUtilizationDiagnoser.class
                                                                                  .getSimpleName(),
                                                                              "under_utilized_node");

  protected ExecutionContext context;

  @Override
  public void initialize(ExecutionContext context) {
    this.context = context;
  }

  @Inject
  public UncommonUtilizationDiagnoser(PolicyConfig policyConfig, Config healthMgrConfig) {
  }

  public Collection<Diagnosis> diagnose(Collection<Symptom> symptoms) {

    ArrayList<Diagnosis> diagnoses = new ArrayList<>();
    SymptomsTable symptomsTable = SymptomsTable.of(symptoms);
    SymptomsTable highCpuUtilization = symptomsTable.type(Utils.getCompositeName(SYMPTOM_HIGH , METRIC_CPU.text()));
    SymptomsTable lowCpuUtilization = symptomsTable.type(Utils.getCompositeName(SYMPTOM_LOW, METRIC_CPU.text()));
    SymptomsTable highMemoryUtilization = symptomsTable.type(Utils.getCompositeName(SYMPTOM_HIGH, METRIC_MEMORY.text()));
    SymptomsTable lowMemoryUtilization = symptomsTable.type(Utils.getCompositeName(SYMPTOM_LOW, METRIC_MEMORY.text()));

    ArrayList<String> overUtilizedNodes = new ArrayList<>();
    for (Symptom highCpuSymptom : highCpuUtilization.get()) {
      for (String node : highCpuSymptom.assignments()) {
        if (highMemoryUtilization.assignment(node).size() > 0) {
          overUtilizedNodes.add(node);
        }
      }
    }

    ArrayList<String> underUtilizedNodes = new ArrayList<>();
    for (Symptom lowCpuSymptom : lowCpuUtilization.get()) {
      for (String node : lowCpuSymptom.assignments()) {
        if (lowMemoryUtilization.assignment(node).size() > 0) {
          underUtilizedNodes.add(node);
        }
      }
    }
    if (overUtilizedNodes.size() > 0) {
      LOG.fine(String.format("Overutilized nodes found: %s", overUtilizedNodes.toString()));
      Diagnosis diagnosis = new Diagnosis(DIAGNOSIS_OVER_UTILIZED_NODE, context.checkpoint(),
                                          overUtilizedNodes);
      diagnoses.add(diagnosis);
    }

    if (underUtilizedNodes.size() > 0) {
      LOG.fine(String.format("Underutilized nodes found: %s", underUtilizedNodes.toString()));
      Diagnosis diagnosis = new Diagnosis(DIAGNOSIS_UNDER_UTILIZED_NODE, context.checkpoint(),
                                          underUtilizedNodes);
      diagnoses.add(diagnosis);
    }

    return diagnoses;
  }
}
