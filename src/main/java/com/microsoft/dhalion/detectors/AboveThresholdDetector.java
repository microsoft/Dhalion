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

/**
 * The AboveThreshold Detector evaluates whether the values of a certain metric are above a user-defined
 * threshold during a time window and in this case returns a symptom. The detector takes as input the metric name,
 * the threshold and the time window specified as the number of latest checkpoints. It generates a
 * SYMPTOM_HIGH_metricName symptom that is parameterized by the metric name.
 *
 * For example, if the threshold is set to 90, the number of latest checkpoints is set to 2 and the metric is set to
 * CPU_UTILIZATION, then the detector will return a symptom SYMPTOM_HIGH_CPU_UTILIZATION only when the CPU_UTILIZATION
 * is above 90 consistently during the last 2 invocations of the policy. By default the number of checkpoints is set
 * to 1 which means that the detector considers only the current value when determining whether to return a symptom.
 */
public class AboveThresholdDetector extends Detector {
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
    this.noCheckpoints = (Double) policyConfig.getConfig(Utils.getCompositeName(ABOVE_THRESHOLD_NO_CHECKPOINTS,
                                                                                metricName), 1);
    this.metricName = metricName;
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
