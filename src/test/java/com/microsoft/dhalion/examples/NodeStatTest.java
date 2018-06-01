package com.microsoft.dhalion.examples;

import com.microsoft.dhalion.core.Measurement;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NodeStatTest {
  private NodeStat statReader = new NodeStat();

  @Test
  public void testParseDataLine1() {
    String line = "NodeA[1]:" +
        "Mem=915MB," +
        "Cpu=11%," +
        "Time=2018-01-08T01:35:36.934Z";
    Set<String> comps = Arrays.stream(new String[]{"NodeA"}).collect(Collectors.toSet());
    Optional<Matcher> m = statReader.getDataMatcher(line);

    assertTrue(m.isPresent());
    assertTrue(m.get().matches());

    validateMetric(line, comps, NodeStat.MEMORY_UTILIZATION, 915);
    validateMetric(line, comps, NodeStat.CPU_UTILIZATION, 11);
    validateMetric(line, comps, NodeStat.NODE_ID, 1);
  }

  private void validateMetric(String line, Set<String> comps, String metricName, double expected) {
    Optional<Measurement> metric = statReader.getMeasurement(line, metricName, comps);
    assertTrue(metric.isPresent());
    assertEquals("1", metric.get().instance());
    assertEquals("NodeA", metric.get().component());
    assertEquals(metricName, metric.get().type());
  }
}
