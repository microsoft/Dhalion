/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.detectors;

import com.microsoft.dhalion.conf.PolicyConfig;
import com.microsoft.dhalion.core.Measurement;
import com.microsoft.dhalion.core.MeasurementsTable;
import com.microsoft.dhalion.core.MeasurementsTable.SortKey;
import com.microsoft.dhalion.core.Symptom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * The AboveThreshold Detector evaluates whether the values of a certain metric are above a user-defined
 * threshold during a time window and in this case returns a symptom. The detector takes as input the metric name,
 * the threshold and the time window specified as the number of latest checkpoints. It generates a
 * {@link AboveThresholdDetector}_metricName symptom that is parameterized by the metric name.
 * <p>
 * For example, if the threshold is set to 90, the number of latest checkpoints is set to 2 and the metric is set to
 * CPU_UTILIZATION, then the detector will return a symptom AboveThresholdDetector_CPU_UTILIZATION only when the
 * CPU_UTILIZATION is above 90 consistently during the last 2 invocations of the policy. By default the number of
 * checkpoints is set to 1 which means that the detector considers only the current value when determining whether to
 * return a symptom.
 */
public class AboveThresholdDetector extends Detector {
  public static final String SYMPTOM_HIGH = AboveThresholdDetector.class.getSimpleName();
  static final String HIGH_THRESHOLD_CONF = "AboveThresholdDetector.threshold";
  static final String ABOVE_THRESHOLD_NO_CHECKPOINTS = "AboveThresholdDetector.noCheckpoints";

  private final double highThreshold;
  private String metricName;
  private double noCheckpoints;

  private static final Logger LOG = Logger.getLogger(AboveThresholdDetector.class.getSimpleName());

  public AboveThresholdDetector(PolicyConfig policyConfig, String metricName) {
    this.highThreshold = (Double) policyConfig.getConfig(String.join("_", HIGH_THRESHOLD_CONF, metricName));
    this.noCheckpoints
        = (Double) policyConfig.getConfig(String.join("_", ABOVE_THRESHOLD_NO_CHECKPOINTS, metricName), 1);
    this.metricName = metricName;
  }

  @Override
  public Collection<Symptom> detect(Collection<Measurement> measurements) {
    if (measurements.isEmpty()) {
      return Collections.emptyList();
    }

    Collection<String> assignments = new ArrayList<>();
    MeasurementsTable measurementsTable = context.measurements().type(metricName).sort(false, SortKey.TIME_STAMP);
    for (String component : measurementsTable.uniqueComponents()) {
      MeasurementsTable componentData = measurementsTable.component(component);
      for (String instance : componentData.uniqueInstances()) {
        MeasurementsTable instanceData = componentData.instance(instance).last((int) noCheckpoints);
        if (instanceData.valueBetween(highThreshold, Double.MAX_VALUE).size() == noCheckpoints) {
          LOG.fine(String.format("Instance %s has values above the limit (%s) for the last %s checkpoints",
                                 instance, highThreshold, noCheckpoints));
          assignments.add(instance);
        }
      }
    }

    if (assignments.isEmpty()) {
      return Collections.emptyList();
    }

    Symptom s = new Symptom(String.join("_", SYMPTOM_HIGH, metricName), context.checkpoint(), assignments);
    return Collections.singletonList(s);
  }
}
