package com.microsoft.dhalion.examples;

import com.microsoft.dhalion.api.IHealthPolicy;
import com.microsoft.dhalion.conf.Config;
import com.microsoft.dhalion.conf.PolicyConfig;
import com.microsoft.dhalion.detectors.AboveThresholdDetector;
import com.microsoft.dhalion.detectors.BelowThresholdDetector;
import com.microsoft.dhalion.policy.HealthPolicyImpl;
import com.microsoft.dhalion.sensors.BasicSensor;

import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.dhalion.examples.MetricName.METRIC_CPU;
import static com.microsoft.dhalion.examples.MetricName.METRIC_MEMORY;

/**
 * This is an example Policy that operates on the example data file. The file contains different types of nodes
 * with their corresponding cpu and memory utilization at different timestamps. The policy alerts the user
 * when a node is over utilized or under utilized.
 */
public class AlertPolicy extends HealthPolicyImpl implements IHealthPolicy {
  private Instant currentCheckPoint;
  private static final Pattern timeData =
      Pattern.compile("((?<Time>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2,4}\\.\\d{2,4})" + "(?<Type>[Z]))");

  @Inject
  public AlertPolicy(PolicyConfig policyConfig,
                     Config sysConfig,
                     CSVMetricsProvider metricsProvider,
                     UncommonUtilizationDiagnoser uncommonUtilizationDiagnoser,
                     AlertResolver alertResolver) {

    Matcher matcher = getDataMatcher("2018-01-08T01:34:36.934Z").get();
    if (matcher != null) {
      currentCheckPoint = getTimestamp(matcher).get();
    }

    BasicSensor cpuUtilizationSensor = new BasicSensor(sysConfig, METRIC_CPU.text(), metricsProvider);
    BasicSensor memoryUtilizationSensor = new BasicSensor(sysConfig, METRIC_MEMORY.text(), metricsProvider);

    AboveThresholdDetector cpuAboveThresholdDetector
        = new AboveThresholdDetector(policyConfig, METRIC_CPU.text());
    AboveThresholdDetector memoryAboveThresholdDetector
        = new AboveThresholdDetector(policyConfig, METRIC_MEMORY.text());
    BelowThresholdDetector cpuBelowThresholdDetector
        = new BelowThresholdDetector(policyConfig, METRIC_CPU.text());
    BelowThresholdDetector memoryBelowThresholdDetector
        = new BelowThresholdDetector(policyConfig, METRIC_MEMORY.text());

    registerSensors(cpuUtilizationSensor, memoryUtilizationSensor);

    registerDetectors(cpuAboveThresholdDetector,
                      memoryAboveThresholdDetector,
                      cpuBelowThresholdDetector,
                      memoryBelowThresholdDetector);

    registerDiagnosers(uncommonUtilizationDiagnoser);
    registerResolvers(alertResolver);

    setPolicyExecutionInterval(policyConfig.interval());
  }

  @Override
  public Instant getNextCheckpoint() {
    currentCheckPoint = currentCheckPoint.plus(1, ChronoUnit.MINUTES);
    return currentCheckPoint;
  }

  Optional<Matcher> getDataMatcher(String time) {
    Matcher tsMatcher = timeData.matcher(time);
    return tsMatcher.matches() ? Optional.of(tsMatcher) : Optional.empty();
  }

  Optional<Instant> getTimestamp(Matcher matcher) {
    Instant currentTime = null;
    if (matcher.group("Time") != null && matcher.group("Type") != null) {
      switch (matcher.group("Type")) {
        case "Z":
          currentTime = Instant.parse(matcher.group("Time") + matcher.group("Type"));
          break;
        default:
          break;
      }
    }
    return Optional.ofNullable(currentTime);
  }
}

