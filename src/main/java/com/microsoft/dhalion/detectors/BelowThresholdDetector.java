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
 * The BelowThreshold Detector evaluates whether the values of a certain metric are below a user-defined
 * threshold during a time window and in this case returns a symptom. The detector takes as input the metric name,
 * the threshold and the time window specified as the number of latest checkpoints. It generates a
 * SYMPTOM_LOW_metricName symptom that is parameterized by the metric name.
 * <p>
 * For example, if the threshold is set to 10, the number of latest checkpoints is set to 2 and the metric is set to
 * CPU_UTILIZATION, then the detector will return a symptom SYMPTOM_LOW_CPU_UTILIZATION only when the CPU_UTILIZATION
 * is below 10 consistently during the last 2 invocations of the policy. By default the number of checkpoints is set
 * to 1 which means that the detector considers only the current value when determining whether to return a symptom.
 */
public class BelowThresholdDetector extends Detector {
  public static String SYMPTOM_LOW = BelowThresholdDetector.class.getSimpleName();

  public static final String LOW_THRESHOLD_CONF = "BelowThresholdDetector.threshold";
  public static final String BELOW_THRESHOLD_NO_CHECKPOINTS = "BelowThresholdDetector.noCheckpoints";

  private final double lowThreshold;
  private String metricName;
  private double noCheckpoints;

  private static final Logger LOG = Logger.getLogger(BelowThresholdDetector.class.getSimpleName());

  @Inject
  public BelowThresholdDetector(PolicyConfig policyConfig, String metricName) {
    this.lowThreshold = (Double) policyConfig.getConfig(Utils.getCompositeName(LOW_THRESHOLD_CONF, metricName));
    this.noCheckpoints = (Double) policyConfig.getConfig(Utils.getCompositeName(BELOW_THRESHOLD_NO_CHECKPOINTS,
                                                                                metricName), 1);
    this.metricName = metricName;
  }

  @Override
  public Collection<Symptom> detect(Collection<Measurement> measurements) {
    ArrayList<Symptom> result = new ArrayList<>();
    if (measurements.size() > 0) {
      MeasurementsTable measurementsTable = context.measurements().type(metricName).sort(false, SortKey.TIME_STAMP);
      Collection<String> belowThresholdAssignments = new ArrayList();
      for (String component : measurementsTable.uniqueComponents()) {
        MeasurementsTable componentData = measurementsTable.component(component);
        for (String instance : componentData.uniqueInstances()) {
          MeasurementsTable instanceData = measurementsTable.instance(instance).last((int) noCheckpoints);
          if (instanceData.valueBetween(Double.MIN_VALUE, lowThreshold).size() == noCheckpoints) {
            LOG.fine(String.format("Instance %s has values below the limit for the last %s checkpoints", instance,
                                   noCheckpoints));
            belowThresholdAssignments.add(instance);
          }
        }
      }
      if (belowThresholdAssignments.size() > 0) {
        Symptom s = new Symptom(Utils.getCompositeName(SYMPTOM_LOW, metricName), context.checkpoint(),
                                belowThresholdAssignments);
        result.add(s);
      }
    }
    return result;
  }
}
