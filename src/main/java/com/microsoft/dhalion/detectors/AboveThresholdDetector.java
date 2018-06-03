package com.microsoft.dhalion.detectors;

import com.microsoft.dhalion.Utils;
import com.microsoft.dhalion.conf.PolicyConfig;
import com.microsoft.dhalion.core.Measurement;
import com.microsoft.dhalion.core.MeasurementsTable;
import com.microsoft.dhalion.core.MeasurementsTable.SortKey;
import com.microsoft.dhalion.core.Symptom;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

public class AboveThresholdDetector extends BaseDetector {
  public static String SYMPTOM_HIGH = AboveThresholdDetector.class.getSimpleName();

  public static final String HIGH_THRESHOLD_CONF = "AboveThresholdDetector.threshold";
  public static final String ABOVE_THRESHOLD_NO_CHECKPOINTS = "AboveThresholdDetector.noCheckpoints";

  private final double highThreshold;
  private String metricName;
  private double noCheckpoints;

  private static final Logger LOG = Logger.getLogger(AboveThresholdDetector.class.getSimpleName());


  @Inject
  public AboveThresholdDetector(PolicyConfig policyConfig, String metricName) {
    this.highThreshold = (Double) policyConfig.getConfig(Utils.getCompositeName(HIGH_THRESHOLD_CONF, metricName));
    this.metricName = metricName;
    this.noCheckpoints = (Double) policyConfig.getConfig(Utils.getCompositeName(ABOVE_THRESHOLD_NO_CHECKPOINTS,
                                                                                metricName), 1);
  }

  @Override
  public Collection<Symptom> detect(Collection<Measurement> measurements) {
    ArrayList<Symptom> result = new ArrayList<>();
    if (measurements.size() > 0) {
      MeasurementsTable measurementsTable = context.measurements().type(metricName).sort(false, SortKey.TIME_STAMP);
      Collection<String> aboveThresholdAssignments = new ArrayList();
      for (String component : measurementsTable.uniqueComponents()) {
        MeasurementsTable componentData = measurementsTable.component(component);
        for (String instance : componentData.uniqueInstances()) {
          MeasurementsTable instanceData = measurementsTable.instance(instance).last((int) noCheckpoints);
          if (instanceData.valueBetween(highThreshold, Double.MAX_VALUE).size() == noCheckpoints) {
            LOG.fine(String.format("Instance %s has values above the limit for the last %s checkpoints", instance,
                                   noCheckpoints));
            aboveThresholdAssignments.add(instance);
          }
        }
      }
      if (aboveThresholdAssignments.size() > 0) {
        Symptom s = new Symptom(Utils.getCompositeName(SYMPTOM_HIGH, metricName), context.checkpoint(),
                                aboveThresholdAssignments);
        result.add(s);
      }
    }
    return result;
  }
}
