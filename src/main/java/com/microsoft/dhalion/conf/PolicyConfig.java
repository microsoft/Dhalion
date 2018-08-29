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
    return (String) get(Key.POLICY_CLASS);
  }

  public Duration interval() {
    return Duration.ofMillis((int) getConfig(Key.POLICY_INTERVAL.value(), 60000));
  }

  private Object get(Key key) {
    return getConfig(key.value());
  }

  public Object getConfig(String configName) {
    return getConfig(configName, null);
  }

  public int intValue(String configKey, int defaultValue) {
    int result = defaultValue;
    if (getConfig(configKey) != null) {
      result = (int) getConfig(configKey);
    }
    return result;
  }

  public String stringValue(String configKey, String defaultValue) {
    String result = defaultValue;
    if (getConfig(configKey) != null) {
      result = (String) getConfig(configKey);
    }
    return result;
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
