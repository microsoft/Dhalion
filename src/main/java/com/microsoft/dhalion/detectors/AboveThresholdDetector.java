package com.microsoft.dhalion.detectors;

import com.microsoft.dhalion.Utils;
import com.microsoft.dhalion.conf.PolicyConfig;
import com.microsoft.dhalion.core.Measurement;
import com.microsoft.dhalion.core.MeasurementsTable;
import com.microsoft.dhalion.core.Symptom;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

public class AboveThresholdDetector extends BaseDetector {
  public static String SYMPTOM_HIGH = AboveThresholdDetector.class.getSimpleName();

  public static final String HIGH_THRESHOLD_CONF = "AboveThresholdDetector.threshold";

  private final double highThreshold;
  private String metricName;

  private static final Logger LOG = Logger.getLogger(AboveThresholdDetector.class.getSimpleName());


  @Inject
  public AboveThresholdDetector(PolicyConfig policyConfig, String metricName) {
    this.highThreshold = (Double) policyConfig.getConfig(Utils.getCompositeName(HIGH_THRESHOLD_CONF, metricName));
    this.metricName = metricName;
  }

  @Override
  public Collection<Symptom> detect(Collection<Measurement> measurements) {
    ArrayList<Symptom> result = new ArrayList<>();
    MeasurementsTable measurementsTable = MeasurementsTable.of(measurements);
    Collection<String> aboveThresholdAssignments = new ArrayList();

    for (String component : measurementsTable.uniqueComponents()) {
      MeasurementsTable componentData = measurementsTable.component(component).type(metricName);
      for (String instance : componentData.uniqueInstances()) {
        if (componentData.instance(instance).get(0).value() > highThreshold) {
          LOG.fine(String.format("Instance %s has value %s above the limit", instance,
                                 componentData.instance(instance).get(0).value()));
          aboveThresholdAssignments.add(instance);
        }
      }
    }
    if (aboveThresholdAssignments.size() > 0) {
      Symptom s = new Symptom(Utils.getCompositeName(SYMPTOM_HIGH, metricName), context.checkpoint(),
                              aboveThresholdAssignments);
      result.add(s);
    }
    return result;
  }
}
