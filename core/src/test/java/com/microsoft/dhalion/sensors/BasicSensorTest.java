package com.microsoft.dhalion.sensors;

import com.microsoft.dhalion.api.MetricsProvider;
import com.microsoft.dhalion.conf.Config;
import com.microsoft.dhalion.conf.Config.ConfigBuilder;
import com.microsoft.dhalion.conf.ConfigName;
import com.microsoft.dhalion.core.Measurement;
import com.microsoft.dhalion.core.MeasurementsTable;
import com.microsoft.dhalion.policy.PoliciesExecutor.ExecutionContext;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BasicSensorTest {
  @Test
  public void testFirstExecution() {
    Instant startTS = Instant.parse("2018-01-08T01:37:36.934Z");

    Config sysConfig = new ConfigBuilder("")
        .put(ConfigName.CONF_COMPONENT_NAMES, "NodeB")
        .build();

    Measurement measurement1 = new Measurement("NodeB", "I1", "cpu", startTS, 2);
    Measurement measurement2 = new Measurement("NodeB", "I2", "cpu", startTS, 4);
    MetricsProvider metricsProvider = mock(MetricsProvider.class);
    when(metricsProvider.getMeasurements(startTS,
                                         Duration.ofMinutes(1),
                                         Collections.singletonList("cpu"),
                                         Collections.singletonList("NodeB")))
        .thenReturn(Arrays.asList(measurement1, measurement2));

    ExecutionContext context = mock(ExecutionContext.class);
    when(context.checkpoint()).thenReturn(startTS);

    BasicSensor sensor = new BasicSensor(sysConfig, "cpu", metricsProvider);
    sensor.initialize(context);

    Collection<Measurement> metrics = sensor.fetch();
    MeasurementsTable metricsTable = MeasurementsTable.of(metrics);
    assertEquals(2, metrics.size());
    assertEquals(2, metricsTable.type("cpu").size());
    assertEquals(1, metricsTable.component("NodeB").instance("I1").size());
    assertEquals(1, metricsTable.component("NodeB").instance("I2").size());

    Set<Double> uniqueIds = metrics.stream().map(Measurement::value).collect(Collectors.toSet());
    assertEquals(2, uniqueIds.size());
  }

  @Test
  public void testDurationBetweenCheckpoints() {
    Instant startTS = Instant.parse("2018-01-08T01:37:36.934Z");

    Config sysConfig = new ConfigBuilder("")
        .put(ConfigName.CONF_COMPONENT_NAMES, "NodeB")
        .build();

    Measurement measurement1 = new Measurement("NodeB", "I1", "cpu", startTS, 2);
    Measurement measurement2 = new Measurement("NodeB", "I2", "cpu", startTS, 4);
    MetricsProvider metricsProvider = mock(MetricsProvider.class);
    when(metricsProvider.getMeasurements(startTS,
                                         Duration.ofMinutes(1),
                                         Collections.singletonList("cpu"),
                                         Collections.singletonList("NodeB")))
        .thenReturn(Arrays.asList(measurement1, measurement2));

    ExecutionContext context = mock(ExecutionContext.class);
    when(context.checkpoint()).thenReturn(startTS);
    when(context.previousCheckpoint()).thenReturn(startTS.minus(1, ChronoUnit.MINUTES));

    BasicSensor sensor = new BasicSensor(sysConfig, "cpu", metricsProvider);
    sensor.initialize(context);

    Collection<Measurement> metrics = sensor.fetch();
    MeasurementsTable metricsTable = MeasurementsTable.of(metrics);
    assertEquals(2, metrics.size());
    assertEquals(2, metricsTable.type("cpu").size());
    assertEquals(1, metricsTable.component("NodeB").instance("I1").size());
    assertEquals(1, metricsTable.component("NodeB").instance("I2").size());

    Set<Double> uniqueIds = metrics.stream().map(Measurement::value).collect(Collectors.toSet());
    assertEquals(2, uniqueIds.size());
  }
}
