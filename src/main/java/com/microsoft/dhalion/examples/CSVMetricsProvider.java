package com.microsoft.dhalion.examples;

import com.microsoft.dhalion.api.MetricsProvider;
import com.microsoft.dhalion.conf.Config;
import com.microsoft.dhalion.conf.ConfigName;
import com.microsoft.dhalion.core.Measurement;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This is an example CSV metrics provider to be used when reading the example data file.
 */
public class CSVMetricsProvider implements MetricsProvider {
  private static final Logger LOG = Logger.getLogger(CSVMetricsProvider.class.getSimpleName());

  private NodeStat nodeStat;
  private List<String> lines;

  @Inject
  public CSVMetricsProvider(Config sysConfig) throws IOException {
    String dataDir = (String) sysConfig.get(ConfigName.DATA_DIR);
    Path dataFile = Paths.get(dataDir, "data.txt");

    if (Files.exists(dataFile)) {
      lines = Files.readAllLines(dataFile);
      LOG.info("Loaded metrics data from " + dataFile);
    } else {
      // try to find the in the classpath
      InputStream stream = CSVMetricsProvider.class.getClassLoader().getResourceAsStream(dataFile.toString());
      if (stream == null) {
        throw new IllegalArgumentException("Missing metrics data file: " + dataFile);
      }

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
        lines = reader.lines().collect(Collectors.toList());
      }

      LOG.info("Loaded metrics resource data from " + dataFile);
    }

    nodeStat = new NodeStat();
  }

  public Collection<Measurement> getMeasurements(Instant startTS,
                                                 Duration duration,
                                                 Collection<String> metrics,
                                                 Collection<String> components) {
    Collection<Measurement> measurements = new ArrayList<>();
    for (String component : components) {
      for (String metric : metrics) {
        measurements.addAll(getMeasurements(metric, startTS, duration, component));
      }
    }
    return measurements;
  }

  private Collection<Measurement> getMeasurements(String metric,
                                                  Instant startTS,
                                                  Duration duration,
                                                  String component) {
    Instant endTS = startTS.minus(duration);
    HashSet<String> componentSet = new HashSet<>();
    componentSet.add(component);

    return lines.stream().map(line -> nodeStat.getMeasurement(line, metric, componentSet))
                .filter(Optional::isPresent).map(Optional::get)
                .filter(reading -> reading.instant().isAfter(endTS))
                .filter(reading -> !reading.instant().isAfter(startTS))
                .collect(Collectors.toList());
  }
}
