/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * This program is made available under the terms of the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package com.microsoft.dhalion.examples;

import com.microsoft.dhalion.api.MetricsProvider;
import com.microsoft.dhalion.conf.Config;
import com.microsoft.dhalion.conf.Key;
import com.microsoft.dhalion.core.Measurement;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * This is an example CSV metrics provider to be used when reading the example data file.
 */
public class CSVMetricsProvider implements MetricsProvider {
  private static final Logger LOG = Logger.getLogger(CSVMetricsProvider.class.getSimpleName());

  private NodeStat nodeStat;
  Config sysConf;

  @Inject
  public CSVMetricsProvider(Config sysConfig) {
    nodeStat = new NodeStat();
    this.sysConf = sysConfig;
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

  private Collection<Measurement> getMeasurements(String metric, Instant startTS, Duration duration, String
      component) {
    Collection<Measurement> metrics = new ArrayList<>();
    Instant endTS = startTS.minus(duration);
    HashSet<String> componentSet = new HashSet<>();
    componentSet.add(component);
    BufferedReader br;
    try {
      br = new BufferedReader(new FileReader(new File(sysConf.get(Key.DATA_DIR.value()).toString(), "data.txt")));
      String line = br.readLine();
      while (line != null) {
        Optional<Measurement> metricData = nodeStat.getMeasurement(line, metric, componentSet);
        if (metricData.isPresent()) {
          if (metricData.get().instant().compareTo(endTS) > 0 && metricData.get().instant().compareTo(startTS) <= 0) {
            metrics.add(metricData.get());
          }
        }
        line = br.readLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return metrics;
  }
}
