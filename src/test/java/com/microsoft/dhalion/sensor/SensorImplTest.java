package com.microsoft.dhalion.sensor;


import com.microsoft.dhalion.api.ISensor;
import com.microsoft.dhalion.metrics.ComponentMetrics;
import com.microsoft.dhalion.metrics.InstanceMetrics;
import org.junit.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class SensorImplTest {
  @Test
  public void testSensorImplGet() {
    ISensor sensor = new SensorImpl("m") {
      @Override
      public Map<String, ComponentMetrics> fetchMetrics() {
        InstanceMetrics instanceMetrics1 = new InstanceMetrics("i1");
        addTestMetrics(instanceMetrics1, "m", 123);

        InstanceMetrics instanceMetrics2 = new InstanceMetrics("i2");
        addTestMetrics(instanceMetrics2, "m", 133);

        InstanceMetrics instanceMetrics3 = new InstanceMetrics("i3");
        addTestMetrics(instanceMetrics3, "m", 143);

        Map<String, InstanceMetrics> instanceMetricsMap1 = new HashMap<>();
        Map<String, InstanceMetrics> instanceMetricsMap2 = new HashMap<>();
        instanceMetricsMap1.put("i1", instanceMetrics1);
        instanceMetricsMap2.put("i2", instanceMetrics2);
        instanceMetricsMap2.put("i3", instanceMetrics3);

        ComponentMetrics componentMetrics1 = new ComponentMetrics("c1", instanceMetricsMap1);
        ComponentMetrics componentMetrics2 = new ComponentMetrics("c2", instanceMetricsMap2);

        metrics.put("c1", componentMetrics1);
        metrics.put("c2", componentMetrics2);
        return metrics;
      }
    };

    sensor.fetchMetrics();
    Map<String, ComponentMetrics> componentData = sensor.getMetrics();

    assertEquals(2, componentData.size());
    assertEquals(1, componentData.get("c1").getInstanceData().size());
    assertEquals(1, componentData.get("c1").getInstanceData().get("i1").getMetrics().size());
    assertNotNull(componentData.get("c1").getInstanceData().get("i1").getMetrics().get("m"));

    assertEquals(2, componentData.get("c2").getInstanceData().size());
    assertEquals(1, componentData.get("c2").getInstanceData().get("i2").getMetrics().size());
    assertEquals(1, componentData.get("c2").getInstanceData().get("i3").getMetrics().size());
    assertNotNull(componentData.get("c2").getInstanceData().get("i2").getMetrics().get("m"));
    assertNotNull(componentData.get("c2").getInstanceData().get("i3").getMetrics().get("m"));

    ComponentMetrics componentMetrics = sensor.getMetrics("c2").orElse(null);
    assertEquals(2, componentMetrics.getInstanceData().size());
    assertEquals(1, componentMetrics.getInstanceData().get("i2").getMetrics().size());
    assertEquals(1, componentMetrics.getInstanceData().get("i3").getMetrics().size());
    componentMetrics = sensor.getMetrics("c1").orElse(null);
    assertEquals(1, componentMetrics.getInstanceData().size());
    assertEquals(1, componentMetrics.getInstanceData().get("i1").getMetrics().size());
  }

  private void addTestMetrics(InstanceMetrics instance, String metricName, int... values) {
    HashMap<Instant, Double> valueMap = new HashMap<>();
    for (int value : values) {
      valueMap.put(Instant.ofEpochSecond(value), (double) value);
    }
    instance.addMetric(metricName, valueMap);
  }
}
