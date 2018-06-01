package com.microsoft.dhalion.sensors;

import com.microsoft.dhalion.conf.Config;
import com.microsoft.dhalion.conf.ConfigBuilder;
import com.microsoft.dhalion.conf.Key;
import com.microsoft.dhalion.core.Measurement;
import com.microsoft.dhalion.core.MeasurementsTable;
import com.microsoft.dhalion.examples.CSVMetricsProvider;
import com.microsoft.dhalion.policy.PoliciesExecutor.ExecutionContext;
import com.microsoft.dhalion.sensors.DirectSensor;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import static com.microsoft.dhalion.examples.MetricName.METRIC_CPU;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DirectSensorTest {

  @Test
  public void testDirectSensor() {
    Instant startTS = Instant.parse("2018-01-08T01:35:36.934Z");

    HashMap<String, Object> conf = new HashMap();
    conf.put("component.names", "NodeA");
    conf.put(Key.DATA_DIR.value(),CSVMetricsProvider.class.getClassLoader().getResource(".").getFile());

    ConfigBuilder cb = new ConfigBuilder("");
    cb.loadConfig(conf);
    Config sysConfig = cb.build();

    CSVMetricsProvider metricsProvider = new CSVMetricsProvider(sysConfig);

    ExecutionContext context = mock(ExecutionContext.class);
    when(context.checkpoint()).thenReturn(startTS);
    when(context.previousCheckpoint()).thenReturn(Instant.MIN);

    DirectSensor sensor = new DirectSensor(null, sysConfig, METRIC_CPU.text(), metricsProvider);
    sensor.initialize(context);

    Collection<Measurement> metrics = sensor.fetch();
    MeasurementsTable metricsTable = MeasurementsTable.of(metrics);
    assertEquals(2, metrics.size());
    assertEquals(2, metricsTable.type(METRIC_CPU.text()).size());
    assertEquals(1, metricsTable.component("NodeA").instance("1").size());
    assertEquals(1, metricsTable.component("NodeA").instance("3").size());

    Set<Double> uniqueIds = metrics.stream().map(Measurement::value).collect(Collectors.toSet());
    assertEquals(2, uniqueIds.size());
  }

  @Test
  public void testDirectSensor2() {
    Instant startTS = Instant.parse("2018-01-08T01:37:36.934Z");
    HashMap<String, Object> conf = new HashMap();
    conf.put("component.names", "NodeB");
    conf.put(Key.DATA_DIR.value(),CSVMetricsProvider.class.getClassLoader().getResource(".").getFile());

    ConfigBuilder cb = new ConfigBuilder("");
    cb.loadConfig(conf);
    Config sysConfig = cb.build();

    CSVMetricsProvider metricsProvider = new CSVMetricsProvider(sysConfig);

    ExecutionContext context = mock(ExecutionContext.class);
    when(context.checkpoint()).thenReturn(startTS);
    when(context.previousCheckpoint()).thenReturn(startTS.minus(1, ChronoUnit.MINUTES));

    DirectSensor sensor = new DirectSensor(null, sysConfig, METRIC_CPU.text(), metricsProvider);
    sensor.initialize(context);

    Collection<Measurement> metrics = sensor.fetch();
    MeasurementsTable metricsTable = MeasurementsTable.of(metrics);
    assertEquals(2, metrics.size());
    assertEquals(2, metricsTable.type(METRIC_CPU.text()).size());
    assertEquals(1, metricsTable.component("NodeB").instance("2").size());
    assertEquals(1, metricsTable.component("NodeB").instance("4").size());

    Set<Double> uniqueIds = metrics.stream().map(Measurement::value).collect(Collectors.toSet());
    assertEquals(2, uniqueIds.size());
  }
}
