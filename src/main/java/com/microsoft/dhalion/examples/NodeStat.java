package com.microsoft.dhalion.examples;

import com.google.common.annotations.VisibleForTesting;
import com.microsoft.dhalion.core.Measurement;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NodeStat {

  static final String MEMORY_UTILIZATION = "Mem";
  static final String CPU_UTILIZATION = "Cpu";
  static final String NODE_ID = "Id";
  static final String TIME = "Time";

  private static final Pattern linePatternData =
      Pattern.compile("^((?<Node>Node.)\\[(?<Id>\\d+)\\]):" + "Mem=(?<Mem>\\d+)MB," + "Cpu=(?<Cpu>[^,]+)%,"
                          + "(Time=(?<Time>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2,4}\\.\\d{2,4})" +
                          "(?<Type>[Z]))");

  @VisibleForTesting
  Optional<Matcher> getDataMatcher(String line) {
    Matcher tsMatcher = linePatternData.matcher(line);
    return tsMatcher.matches() ? Optional.of(tsMatcher) : Optional.empty();
  }

  Optional<Measurement> getMeasurement(String line, String metric, Set<String> components) {
    Matcher matcher = getDataMatcher(line).orElse(null);
    if (matcher == null) {
      return Optional.empty();
    }

    String nodeType = matcher.group("Node");
    if (!components.contains(nodeType)) {
      return Optional.empty();
    }

    String nodeId = matcher.group(NODE_ID);
    double value = -1;
    Instant instant = getTimestamp(matcher).get();
    if (matcher.group(metric) != null && getTimestamp(matcher).get().equals(instant)) {
      switch (metric) {
        case MEMORY_UTILIZATION:
          value = Double.parseDouble(matcher.group(MEMORY_UTILIZATION));
          break;
        case CPU_UTILIZATION:
          value = Double.parseDouble(matcher.group(CPU_UTILIZATION));
          break;
      }
    }
    Measurement measurement = new Measurement(nodeType, nodeId, metric, instant, value);
    return Optional.of(measurement);
  }

  Optional<Instant> getTimestamp(Matcher matcher) {
    Instant currentTime = null;
    if (matcher.group(TIME) != null && matcher.group("Type") != null) {
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
