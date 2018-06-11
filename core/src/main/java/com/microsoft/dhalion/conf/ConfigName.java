package com.microsoft.dhalion.conf;

public enum ConfigName {
  CONF_DIR("healthmgr.conf.dir"),
  DATA_DIR("healthmgr.data.dir"),

  HEALTHMGR_CONF("healthmgr.config.filename", "healthmgr.yaml"),
  POLICY_CONF("healthmgr.policy.conf.filename", "policyconf.yaml"),
  HEALTHMGR_MODE("healthmgr.mode", "online"),

  // health policy config keys
  POLICIES("health.policies"),
  POLICY_CLASS("health.policy.class"),
  POLICY_INTERVAL("health.policy.interval.ms"),
  POLICY_CONF_SENSOR_DURATION_SUFFIX(".duration"),
  CONF_COMPONENT_NAMES("component.names"),

  METRICS_PROVIDER_CLASS("metrics.provider.class");

  private final String configName;
  private final Object defaultValue;

  ConfigName(String configName) {
    this(configName, null);
  }

  ConfigName(String configName, Object defaultValue) {
    this.configName = configName;
    this.defaultValue = defaultValue;
  }

  /**
   * Get the key configName for this enum
   *
   * @return key configName
   */
  public String configName() {
    return configName;
  }

  /**
   * Return the default configName
   */
  public Object getDefault() {
    return this.defaultValue;
  }
}
