package com.microsoft.dhalion.conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;


public class Config {
  private static final Logger LOG = Logger.getLogger(Config.class.getName());

  private final Map<String, Object> cfgMap;
  private final Set<PolicyConfig> policyConf;

  protected Config(ConfigBuilder builder) {
    this.cfgMap = new HashMap<>(builder.getKeyValues());
    this.policyConf = new HashSet<>(builder.getPolicyConf());
  }

  public List<PolicyConfig> policies() {
    return new ArrayList<>(policyConf);
  }

  public HealthManagerMode mode() {
    return HealthManagerMode.valueOf((String) get(Key.HEALTHMGR_MODE));
  }

  private Object get(Key key) {
    return get(key, key.getDefault());
  }

  public Object get(String name) {
    return cfgMap.get(name);
  }

  private Object get(Key key, Object defaultValue) {
    Object value = cfgMap.get(key.value());
    if (value == null) {
      value = defaultValue;
    }
    return value;
  }

  public Map<String, Object> getCfgMap() {
    return cfgMap;
  }

  public Set<PolicyConfig> getPolicyConf() {
    return policyConf;
  }

  public enum HealthManagerMode {
    offline,
    online
  }
}
