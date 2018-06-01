package com.microsoft.dhalion.sensors;

import com.microsoft.dhalion.api.ISensor;
import com.microsoft.dhalion.api.MetricsProvider;
import com.microsoft.dhalion.conf.PolicyConfig;
import com.microsoft.dhalion.policy.PoliciesExecutor.ExecutionContext;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;

public class BaseSensor implements ISensor{

  protected ExecutionContext context;
  protected final PolicyConfig policyConf;
  protected final MetricsProvider metricsProvider;
  protected String metricName;

  @Inject
  BaseSensor(PolicyConfig policyConf,
               String metricName,
               MetricsProvider metricsProvider) {
    this.metricName = metricName;
    this.policyConf = policyConf;
    this.metricsProvider = metricsProvider;
  }

  @Override
  public void initialize(ExecutionContext context) {
    this.context = context;
  }

  /**
   * Returns the metric names that the sensor operates on
   * @return metricName
   */
  @Override
  public Collection<String> getMetricTypes() {
    return Collections.singletonList(metricName);
  }



}
