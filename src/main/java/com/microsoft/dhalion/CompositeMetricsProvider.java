package com.microsoft.dhalion;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.microsoft.dhalion.api.MetricsProvider;
import com.microsoft.dhalion.conf.Config;
import com.microsoft.dhalion.conf.Key;
import com.microsoft.dhalion.core.Measurement;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CompositeMetricsProvider implements MetricsProvider {

  private List<MetricsProvider> metricsProviders;
  private Map<String, MetricsProvider> metricsProviderMap;
  protected Config sysConfig;

  @Inject
  public CompositeMetricsProvider(Config sysConf) throws ClassNotFoundException {
    sysConfig = sysConf;
    metricsProviders = new ArrayList<>();
    metricsProviderMap = new HashMap<>();
    instantiateMetricsProviders();
  }

  @VisibleForTesting
  protected void instantiateMetricsProviders() throws ClassNotFoundException {
    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Config.class).toInstance(sysConfig);
      }
    });
    String metricsProviderClasses = (String) sysConfig.get(Key.METRICS_PROVIDER_CLASS.value());
    if (!metricsProviderClasses.isEmpty()) {
      String[] mpClasses = metricsProviderClasses.split(",");
      for (String mpClass : mpClasses) {
        Class<MetricsProvider> metricsProviderClass =
            (Class<MetricsProvider>) this.getClass().getClassLoader().loadClass(mpClass);
        addMetricsProvider(injector.getInstance(metricsProviderClass));
      }
    }
  }

  protected void addMetricsProvider(MetricsProvider provider) {
    metricsProviders.add(provider);
  }

  @Override
  public void initialize() {
    for (MetricsProvider metricsProvider : metricsProviders) {
      metricsProvider.initialize();
      Set<String> metricTypes = metricsProvider.getMetricTypes();
      if (metricTypes != null) {
        for (String metricType : metricsProvider.getMetricTypes()) {
          metricsProviderMap.put(metricType, metricsProvider);
        }
      }
    }
  }

  @Override
  public Collection<Measurement> getMeasurements(Instant startTime,
      Duration duration, Collection<String> metrics,
      Collection<String> components) {
    Collection<Measurement> measurements = new ArrayList<>();
    for (String metric : metrics) {
      MetricsProvider provider = metricsProviderMap.get(metric);
      if (provider != null) {
        measurements.addAll(provider.getMeasurements(startTime, duration,
            Collections.singletonList(metric), components));
      }
    }
    return measurements;
  }

  @Override
  public Set<String> getMetricTypes() {
    return metricsProviderMap.keySet();
  }

  @Override
  public Collection<Measurement> getMeasurements(Instant startTime,
      Duration duration, String metric, String component) {
    MetricsProvider provider = metricsProviderMap.get(metric);
    if (provider != null) {
      return provider.getMeasurements(startTime, duration, metric, component);
    }
    return null;
  }

  @Override
  public void close() {
    for (MetricsProvider metricsProvider : metricsProviders) {
      metricsProvider.close();
    }
  }
}
