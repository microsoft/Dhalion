package com.microsoft.dhalion;

import com.microsoft.dhalion.api.MetricsProvider;
import com.microsoft.dhalion.conf.Config;
import com.microsoft.dhalion.conf.Key;
import com.microsoft.dhalion.core.Measurement;
import com.microsoft.dhalion.core.MeasurementsTable;
import com.microsoft.dhalion.examples.CSVMetricsProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.microsoft.dhalion.core.MeasurementsTable.SortKey.TIME_STAMP;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompositeMetricsProviderTest {

  private MetricsProvider provider;

  @Before
  public void setup() throws ClassNotFoundException {
    Config conf = mock(Config.class);
    when(conf.get(Key.DATA_DIR.value())).thenReturn(CompositeMetricsProvider.class.getClassLoader().getResource(".").getFile());
    provider = new MockCompositeMetricsProviderTest(conf);
    provider.initialize();
  }

  @Test
  public void testGetMetricTypes() {
    Set<String> metricTypes = provider.getMetricTypes();
    Assert.assertEquals(metricTypes.size(), 3);
    Assert.assertTrue(metricTypes.contains("MockMetric"));
    Assert.assertTrue(metricTypes.contains("Cpu"));
  }

  @Test
  public void testGetMeasurements() {
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

    metrics = MeasurementsTable.of(
        provider.getMeasurements(startTS, duration, "MockMetric", "abc"));
    assertEquals(metrics.size(), 1);

    Assert.assertNull(
        provider.getMeasurements(startTS, duration, "WrongMetric", "abc"));
  }

  class MockCompositeMetricsProviderTest extends CompositeMetricsProvider {
    MockCompositeMetricsProviderTest(Config sysConf) throws ClassNotFoundException {
      super(sysConf);
    }

    @Override
    protected void instantiateMetricsProviders() throws ClassNotFoundException {
      addMetricsProvider(new CSVMetricsProvider(sysConfig));
      addMetricsProvider(new MockMetricsProvider());
    }
  }

  class MockMetricsProvider implements MetricsProvider {
    @Override public Collection<Measurement> getMeasurements(Instant startTime,
        Duration duration, Collection<String> metrics,
        Collection<String> components) {
      List<Measurement> measurements = new ArrayList<>();
      for (String component : components) {
        for (String metric : metrics) {
          measurements.addAll(getMeasurements(startTime, duration, metric, component));
        }
      }
      return measurements;
    }

    @Override public Set<String> getMetricTypes() {
      return Collections.singleton("MockMetric");
    }

    @Override public Collection<Measurement> getMeasurements(Instant startTime,
        Duration duration, String metric, String component) {
      return Collections.singleton(new Measurement(component, "1", metric, startTime, 100));
    }
  }
}
