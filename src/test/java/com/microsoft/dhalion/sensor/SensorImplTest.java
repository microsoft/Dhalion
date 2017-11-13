package com.microsoft.dhalion.sensor;


import com.microsoft.dhalion.api.ISensor;
import com.microsoft.dhalion.metrics.ComponentMetrics;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SensorImplTest {
  @Test
  public void testSensorImplGet() {
    ISensor sensor = new SensorImpl("m") {
      @Override
      public ComponentMetrics fetchMetrics() {
        metrics.addMetric("c1", "i1", "m", 123);
        metrics.addMetric("c1", "i2", "m", 123);
        metrics.addMetric("c2", "i3", "m", 133);
        metrics.addMetric("c2", "i4", "m", 143);
        return metrics;
      }
    };

    assertEquals("m", sensor.getMetricName());
    sensor.fetchMetrics();
    ComponentMetrics componentData = sensor.readMetrics();

    assertEquals(2, componentData.getComponentNames().size());
    assertEquals(2, componentData.filterByComponent("c1").getMetrics().size());
    assertEquals(1, componentData.filterByInstance("c1", "i1").getMetrics().size());
  }
}
