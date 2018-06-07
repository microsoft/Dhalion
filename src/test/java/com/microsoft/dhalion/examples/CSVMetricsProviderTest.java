package com.microsoft.dhalion.examples;

import com.microsoft.dhalion.conf.Config;
import com.microsoft.dhalion.conf.ConfigName;
import com.microsoft.dhalion.core.Measurement;
import com.microsoft.dhalion.core.MeasurementsTable;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static com.microsoft.dhalion.core.MeasurementsTable.SortKey.TIME_STAMP;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CSVMetricsProviderTest {
  private CSVMetricsProvider provider;

  @Test
  public void testCSVMetricsProvider() throws Exception {
    Config conf = mock(Config.class);
    when(conf.get(ConfigName.DATA_DIR))
        .thenReturn(CSVMetricsProvider.class.getClassLoader().getResource(".").getFile());
    provider = new CSVMetricsProvider(conf);

    Instant startTS = Instant.parse("2018-01-08T01:37:36.934Z");
    String metric = "Cpu";
    Duration duration = Duration.ofMinutes(2);
    String comp = "NodeA";

    Collection<String> metricNames = new ArrayList<>();
    metricNames.add(metric);
    Collection<String> components = new ArrayList<>();
    components.add(comp);

    MeasurementsTable metrics = MeasurementsTable.of(
        provider.getMeasurements(startTS, duration, metricNames, components));
    assertEquals(4, metrics.size());
    assertEquals(4, metrics.type(metric).size());
    assertEquals(2, metrics.component(comp).instance("1").size());
    assertEquals(2, metrics.component(comp).instance("3").size());

    Iterator<Measurement> measurements = metrics.component(comp).instance("1").sort(false, TIME_STAMP).get().iterator();
    assertEquals("2018-01-08T01:36:36.934Z", measurements.next().instant().toString());
    assertEquals("2018-01-08T01:37:36.934Z", measurements.next().instant().toString());
  }

  @Test
  public void testCSVMetricsProvider2() throws Exception {
    Config conf = mock(Config.class);
    when(conf.get(ConfigName.DATA_DIR))
        .thenReturn(CSVMetricsProvider.class.getClassLoader().getResource(".").getFile());
    provider = new CSVMetricsProvider(conf);

    Instant startTS = Instant.parse("2018-01-08T01:37:36.934Z");
    String metric = "Mem";
    Duration duration = Duration.ofMinutes(2);
    String comp = "NodeB";

    MeasurementsTable metrics = MeasurementsTable.of(provider.getMeasurements(startTS, duration, metric, comp));
    assertEquals(4, metrics.size());
    assertEquals(4, metrics.type(metric).size());
    assertEquals(2, metrics.component(comp).instance("2").size());
    assertEquals(2, metrics.component(comp).instance("4").size());

    Iterator<Measurement> measurements = metrics.component(comp).instance("2").sort(false, TIME_STAMP).get()
                                                .iterator();
    assertEquals("2018-01-08T01:36:36.934Z", measurements.next().instant().toString());
    assertEquals("2018-01-08T01:37:36.934Z", measurements.next().instant().toString());
  }
}
