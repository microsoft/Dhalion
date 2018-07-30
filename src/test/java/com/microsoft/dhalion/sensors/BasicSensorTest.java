/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.sensors;

import com.microsoft.dhalion.api.MetricsProvider;
import com.microsoft.dhalion.conf.Config;
import com.microsoft.dhalion.conf.ConfigBuilder;
import com.microsoft.dhalion.conf.Key;
import com.microsoft.dhalion.core.Measurement;
import com.microsoft.dhalion.core.MeasurementsTable;
import com.microsoft.dhalion.examples.CSVMetricsProvider;
import com.microsoft.dhalion.policy.PoliciesExecutor.ExecutionContext;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import static com.microsoft.dhalion.examples.MetricName.METRIC_CPU;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BasicSensorTest {
  @Test
  public void testFirstExecution() {
    Instant startTS = Instant.parse("2018-01-08T01:37:36.934Z");

    Config sysConfig = new ConfigBuilder("")
        .put(Key.CONF_COMPONENT_NAMES, "NodeB")
        .build();

    Measurement measurement1 = new Measurement("NodeB", "I1", METRIC_CPU.text(), startTS, 2);
    Measurement measurement2 = new Measurement("NodeB", "I2", METRIC_CPU.text(), startTS, 4);
    MetricsProvider metricsProvider = mock(MetricsProvider.class);
    when(metricsProvider.getMeasurements(startTS,
                                         Duration.ofMinutes(1),
                                         Collections.singletonList(METRIC_CPU.text()),
                                         Collections.singletonList("NodeB")))
        .thenReturn(Arrays.asList(measurement1, measurement2));

    ExecutionContext context = mock(ExecutionContext.class);
    when(context.checkpoint()).thenReturn(startTS);

    BasicSensor sensor = new BasicSensor(sysConfig, METRIC_CPU.text(), metricsProvider);
    sensor.initialize(context);

    Collection<Measurement> metrics = sensor.fetch();
    MeasurementsTable metricsTable = MeasurementsTable.of(metrics);
    assertEquals(2, metrics.size());
    assertEquals(2, metricsTable.type(METRIC_CPU.text()).size());
    assertEquals(1, metricsTable.component("NodeB").instance("I1").size());
    assertEquals(1, metricsTable.component("NodeB").instance("I2").size());

    Set<Double> uniqueIds = metrics.stream().map(Measurement::value).collect(Collectors.toSet());
    assertEquals(2, uniqueIds.size());
  }

  @Test
  public void testDurationBetweenCheckpoints() {
    Instant startTS = Instant.parse("2018-01-08T01:37:36.934Z");

    Config sysConfig = new ConfigBuilder("")
        .put(Key.CONF_COMPONENT_NAMES, "NodeB")
        .build();

    Measurement measurement1 = new Measurement("NodeB", "I1", METRIC_CPU.text(), startTS, 2);
    Measurement measurement2 = new Measurement("NodeB", "I2", METRIC_CPU.text(), startTS, 4);
    MetricsProvider metricsProvider = mock(MetricsProvider.class);
    when(metricsProvider.getMeasurements(startTS,
                                         Duration.ofMinutes(1),
                                         Collections.singletonList(METRIC_CPU.text()),
                                         Collections.singletonList("NodeB")))
        .thenReturn(Arrays.asList(measurement1, measurement2));

    ExecutionContext context = mock(ExecutionContext.class);
    when(context.checkpoint()).thenReturn(startTS);
    when(context.previousCheckpoint()).thenReturn(startTS.minus(1, ChronoUnit.MINUTES));

    BasicSensor sensor = new BasicSensor(sysConfig, METRIC_CPU.text(), metricsProvider);
    sensor.initialize(context);

    Collection<Measurement> metrics = sensor.fetch();
    MeasurementsTable metricsTable = MeasurementsTable.of(metrics);
    assertEquals(2, metrics.size());
    assertEquals(2, metricsTable.type(METRIC_CPU.text()).size());
    assertEquals(1, metricsTable.component("NodeB").instance("I1").size());
    assertEquals(1, metricsTable.component("NodeB").instance("I2").size());

    Set<Double> uniqueIds = metrics.stream().map(Measurement::value).collect(Collectors.toSet());
    assertEquals(2, uniqueIds.size());
  }
}
