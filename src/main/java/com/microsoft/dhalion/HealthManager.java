package com.microsoft.dhalion;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.microsoft.dhalion.api.IHealthPolicy;
import com.microsoft.dhalion.api.MetricsProvider;
import com.microsoft.dhalion.conf.Config;
import com.microsoft.dhalion.conf.Config.ConfigBuilder;
import com.microsoft.dhalion.conf.ConfigName;
import com.microsoft.dhalion.conf.PolicyConfig;
import com.microsoft.dhalion.policy.PoliciesExecutor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

public class HealthManager {
  public static final String CONF_DIR = "conf.dir";

  private static final Logger LOG = Logger.getLogger(HealthManager.class.getName());
  private final String configDir;

  private Injector injector;
  private List<IHealthPolicy> healthPolicies = new ArrayList<>();
  private final MetricsProvider metricsProvider;

  public static void main(String[] args) throws Exception {
    CommandLineParser parser = new DefaultParser();
    Options slaManagerCliOptions = constructCliOptions();

    // parse the help options first.
    Options helpOptions = constructHelpOptions();
    CommandLine cmd = parser.parse(helpOptions, args, true);
    if (cmd.hasOption("h")) {
      usage(slaManagerCliOptions);
      return;
    }

    try {
      cmd = parser.parse(slaManagerCliOptions, args);
    } catch (ParseException e) {
      usage(slaManagerCliOptions);
      throw new RuntimeException("Error parsing command line options: ", e);
    }

    LOG.info("Initializing the health manager");
    HealthManager healthmgr = new HealthManager(cmd);
    healthmgr.start();
  }

  private HealthManager(CommandLine cmd) throws ClassNotFoundException {
    this.configDir = cmd.getOptionValue(CONF_DIR, "");
    if (!Files.isDirectory(Paths.get(configDir))) {
      throw new IllegalArgumentException("The config path provided must be a directory: " + configDir);
    }

    //Read healthmgr.yaml and create a hashmap with the configurations
    ConfigBuilder confBuilder = new ConfigBuilder(configDir);
    final Config sysConfig = confBuilder
        .loadConfig(Paths.get(configDir, (String) ConfigName.HEALTHMGR_CONF.getDefault()))
        .build();

    AbstractModule module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(String.class).annotatedWith(Names.named(CONF_DIR)).toInstance(configDir);
        bind(Config.class).toInstance(sysConfig);
      }
    };

    injector = Guice.createInjector(module);

    //Read the MetricsProvider class
    String metricsProviderClass = (String) sysConfig.get(ConfigName.METRICS_PROVIDER_CLASS);
    Class<MetricsProvider> mpClass
        = (Class<MetricsProvider>) this.getClass().getClassLoader().loadClass(metricsProviderClass);
    injector = injector.createChildInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(mpClass).in(Singleton.class);
      }
    });

    metricsProvider = injector.getInstance(mpClass);

    injector = injector.createChildInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(MetricsProvider.class).toInstance(metricsProvider);
      }
    });

    Set<PolicyConfig> policyConfs = confBuilder.loadPolicyConf();
    initializePolicies(policyConfs);
  }


  private void initializePolicies(Set<PolicyConfig> policyConfs) throws ClassNotFoundException {
    for (PolicyConfig policyConf : policyConfs) {
      String policyClassName = policyConf.policyClass();
      LOG.info(String.format("Initializing %s with class %s", policyConf.id(), policyClassName));
      Class<IHealthPolicy> policyClass
          = (Class<IHealthPolicy>) this.getClass().getClassLoader().loadClass(policyClassName);

      AbstractModule module = constructPolicySpecificModule(policyConf);
      IHealthPolicy policy = injector.createChildInjector(module).getInstance(policyClass);

      healthPolicies.add(policy);
    }
  }

  private AbstractModule constructPolicySpecificModule(final PolicyConfig policyConfig) {
    return new AbstractModule() {
      @Override
      protected void configure() {
        bind(PolicyConfig.class).toInstance(policyConfig);
      }
    };
  }

  private void start() throws InterruptedException, ExecutionException {
    LOG.info("Starting Health Manager");
    PoliciesExecutor policyExecutor = new PoliciesExecutor(healthPolicies);
    ScheduledFuture<?> future = policyExecutor.start();
    try {
      future.get();
    } finally {
      policyExecutor.destroy();
      metricsProvider.close();
    }
  }

  private static Options constructCliOptions() {
    Options options = new Options();

    Option configFile = Option.builder("p")
                              .desc("Path of the config files")
                              .longOpt(CONF_DIR)
                              .hasArgs()
                              .argName("config path")
                              .build();

    options.addOption(configFile);
    return options;
  }

  // construct command line help options
  private static Options constructHelpOptions() {
    Options options = new Options();
    Option help = Option.builder("h")
                        .desc("List all options and their description")
                        .longOpt("help")
                        .build();

    options.addOption(help);
    return options;
  }

  // Print usage options
  private static void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(HealthManager.class.getSimpleName(), options);
  }

}
