package com.microsoft.dhalion.conf;


import java.time.Duration;
import java.util.Map;


public class PolicyConfig {
  private final String policyId;
  private final Map<String, Object> configs;

  public PolicyConfig(String policy, Map<String, Object> configs) {
    this.policyId = policy;
    this.configs = configs;
  }

  public String id() {
    return policyId;
  }

  public String policyClass() {
    return (String) get(ConfigName.POLICY_CLASS);
  }

  public Duration interval() {
    return Duration.ofMillis((int) getConfig(ConfigName.POLICY_INTERVAL.configName(), 60000));
  }

  private Object get(ConfigName configName) {
    return getConfig(configName.configName());
  }

  public Object getConfig(String configName) {
    return getConfig(configName, null);
  }

  public Object getConfig(String configName, Object defaultValue) {
    Object value = configs.get(configName);
    if (value == null) {
      value = defaultValue;
    }
    return value;
  }

  @Override
  public String toString() {
    return configs.toString();
  }
}
