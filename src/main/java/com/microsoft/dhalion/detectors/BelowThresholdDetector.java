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

public class BelowThresholdDetector extends BaseDetector {
  public static String SYMPTOM_LOW = BelowThresholdDetector.class.getSimpleName();

  public static final String LOW_THRESHOLD_CONF = "BelowThresholdDetector.threshold";

  private final double lowThreshold;
  private String metricName;

  private static final Logger LOG = Logger.getLogger(BelowThresholdDetector.class.getSimpleName());

  @Inject
  public BelowThresholdDetector(PolicyConfig policyConfig, String metricName) {
    this.lowThreshold = (Double) policyConfig.getConfig(Utils.getCompositeName(LOW_THRESHOLD_CONF, metricName));
    this.metricName = metricName;
  }

  @Override
  public Collection<Symptom> detect(Collection<Measurement> measurements) {
    ArrayList<Symptom> result = new ArrayList<>();
    MeasurementsTable measurementsTable = MeasurementsTable.of(measurements);
    Collection<String> belowThresholdAssignments = new ArrayList();

    for (String component : measurementsTable.uniqueComponents()) {
      MeasurementsTable componentData = measurementsTable.component(component).type(metricName);
      for (String instance : componentData.uniqueInstances()) {
        if (componentData.instance(instance).get(0).value() < lowThreshold) {
          LOG.fine(String.format("Instance %s has value %s below the limit", instance,
                                 componentData.instance(instance).get(0).value()));
          belowThresholdAssignments.add(instance);
        }
      }
    }
    if (belowThresholdAssignments.size() > 0) {
      Symptom s = new Symptom(Utils.getCompositeName(SYMPTOM_LOW, metricName), context.checkpoint(),
                              belowThresholdAssignments);
      result.add(s);
    }
    return result;
  }
}
