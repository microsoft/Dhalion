package com.microsoft.dhalion.sensor;


import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.dhalion.metrics.ComponentMetrics;
import com.microsoft.dhalion.metrics.InstanceMetrics;
import com.microsoft.dhalion.state.State;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SensorImplTest {

  @Test
  public void testSensorImplGet() {
    Map<String, InstanceMetrics> instanceMetricsMap1 = new HashMap<>();

    InstanceMetrics instanceMetrics1 = new InstanceMetrics("i1");
    addTestMetrics(instanceMetrics1, "m1", 123);
    addTestMetrics(instanceMetrics1, "m2", 223);
    addTestMetrics(instanceMetrics1, "m3", 323);

    instanceMetricsMap1.put("i1", instanceMetrics1);
    ComponentMetrics componentMetrics1 = new ComponentMetrics("c1", instanceMetricsMap1);

    Map<String, InstanceMetrics> instanceMetricsMap2 = new HashMap<>();

    InstanceMetrics instanceMetrics2 = new InstanceMetrics("i2");
    addTestMetrics(instanceMetrics2, "m1", 133);
    addTestMetrics(instanceMetrics2, "m2", 233);
    addTestMetrics(instanceMetrics2, "m3", 333);

    InstanceMetrics instanceMetrics3 = new InstanceMetrics("i3");
    addTestMetrics(instanceMetrics3, "m1", 133);
    addTestMetrics(instanceMetrics3, "m2", 233);
    addTestMetrics(instanceMetrics3, "m3", 333);

    instanceMetricsMap2.put("i2", instanceMetrics2);
    instanceMetricsMap2.put("i3", instanceMetrics3);

    ComponentMetrics componentMetrics2 = new ComponentMetrics("c2", instanceMetricsMap2);

    HashMap<String, ComponentMetrics> metrics = new HashMap<>();
    metrics.put("c1", componentMetrics1);
    metrics.put("c2", componentMetrics2);
    State snapshot = new State();
    snapshot.initialize();
    snapshot.addToState(metrics);

    SensorImpl sensor = new SensorImpl("m2");
    sensor.initialize(snapshot);

    Map<String, ComponentMetrics> componentData = sensor.get("c1", "c2");
    assertEquals(2, componentData.size());
    assertEquals(1, componentData.get("c1").getInstanceData().size());
    assertEquals(1, componentData.get("c1").getInstanceData().get("i1").getMetrics().size());
    assertNull(componentData.get("c1").getInstanceData().get("i1").getMetrics().get("m1"));
    assertNull(componentData.get("c1").getInstanceData().get("i1").getMetrics().get("m3"));
    assertNotNull(componentData.get("c1").getInstanceData().get("i1").getMetrics().get("m2"));

    assertEquals(2, componentData.get("c2").getInstanceData().size());
    assertEquals(1, componentData.get("c2").getInstanceData().get("i2").getMetrics().size());
    assertEquals(1, componentData.get("c2").getInstanceData().get("i3").getMetrics().size());
    assertNull(componentData.get("c2").getInstanceData().get("i2").getMetrics().get("m1"));
    assertNull(componentData.get("c2").getInstanceData().get("i2").getMetrics().get("m3"));
    assertNull(componentData.get("c2").getInstanceData().get("i3").getMetrics().get("m1"));
    assertNull(componentData.get("c2").getInstanceData().get("i3").getMetrics().get("m3"));
    assertNotNull(componentData.get("c2").getInstanceData().get("i2").getMetrics().get("m2"));
    assertNotNull(componentData.get("c2").getInstanceData().get("i3").getMetrics().get("m2"));


  }

  private void addTestMetrics(InstanceMetrics instance, String metricName, int... values) {
    HashMap<Instant, Double> valueMap = new HashMap<>();
    for (int value : values) {
      valueMap.put(Instant.ofEpochSecond(value), (double) value);

    }
    instance.addMetric(metricName, valueMap);
  }
}
