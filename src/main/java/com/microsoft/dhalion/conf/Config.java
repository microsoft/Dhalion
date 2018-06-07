package com.microsoft.dhalion.conf;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class Config {
  private static final Logger LOG = Logger.getLogger(Config.class.getName());
  private final Map<String, Object> cfgMap;

  private Config(ConfigBuilder builder) {
    this.cfgMap = new HashMap<>(builder.keyValues);
    if (LOG.isLoggable(Level.FINE)) {
      cfgMap.forEach((k, v) -> LOG.fine(k + " : " + v));
    }
  }

  public HealthManagerMode mode() {
    return HealthManagerMode.valueOf((String) get(ConfigName.HEALTHMGR_MODE));
  }

  public Object get(ConfigName configName) {
    return get(configName, configName.getDefault());
  }

  public Object get(String name) {
    return cfgMap.get(name);
  }

  private Object get(ConfigName configName, Object defaultValue) {
    Object value = cfgMap.get(configName.configName());
    if (value == null) {
      value = defaultValue;
    }
    return value;
  }

  public Map<String, Object> getConfigs() {
    return new HashMap<>(cfgMap);
  }

  public enum HealthManagerMode {
    offline,
    online
  }

  public static class ConfigBuilder {
    private static final Logger LOG = Logger.getLogger(ConfigBuilder.class.getName());

    private final Map<String, Object> keyValues = new HashMap<>();
    private final String confDir;

    public ConfigBuilder(String confDir) {
      this.confDir = confDir;
      loadDefaults();
    }

    private void loadDefaults() {
      Arrays.stream(ConfigName.values()).filter(k -> k.getDefault() != null).forEach(k -> put(k, k.getDefault()));
      put(ConfigName.CONF_DIR, confDir);
    }

    public Set<PolicyConfig> loadPolicyConf() {
      Path policyConfigFile = Paths.get(confDir, (String) keyValues.get(ConfigName.POLICY_CONF.configName()));
      Map<String, Object> ret = loadFile(policyConfigFile);
      return getListOfStrings(ret.get(ConfigName.POLICIES.configName()))
          .stream().map(id -> new PolicyConfig(id, (Map<String, Object>) ret.get(id))).collect(Collectors.toSet());
    }

    public ConfigBuilder loadConfig(Path file) {
      Map<String, Object> readConfig = loadFile(file);
      return loadConfig(readConfig);
    }

    public ConfigBuilder put(String key, Object value) {
      this.keyValues.put(key, value);
      return this;
    }

    public ConfigBuilder put(ConfigName configName, Object value) {
      put(configName.configName(), value);
      return this;
    }

    private ConfigBuilder loadConfig(Map<String, Object> map) {
      keyValues.putAll(map);
      return this;
    }

    public Config build() {
      return new Config(this);
    }

    private static Map<String, Object> loadFile(Path file) {
      InputStream confInStream;

      // check if the file exists and also it is a regular file
      if (Files.exists(file) && Files.isRegularFile(file)) {
        LOG.info(String.format("Loading conf from %s", file));
        try {
          confInStream = new FileInputStream(file.toFile());
        } catch (FileNotFoundException e) {
          throw new RuntimeException("Failed to read " + file, e);
        }
      } else {
        confInStream = ConfigBuilder.class.getClassLoader().getResourceAsStream(file.toString());
        if (confInStream != null) {
          LOG.info(String.format("Loading resource conf from %s", file));
        }
      }

      if (confInStream == null) {
        LOG.warning("Unable to load conf from " + file);
        return new HashMap<>();
      }

      try (InputStream in = confInStream) {
        Yaml yaml = new Yaml();
        Map<String, Object> propsYaml = yaml.load(in);
        LOG.log(Level.INFO, "Successfully read config file {0}", file);
        return propsYaml;
      } catch (IOException e) {
        throw new RuntimeException("Failed to read from conf file stream", e);
      }
    }

    private static List<String> getListOfStrings(Object o) {
      if (o == null) {
        return new ArrayList<>();
      } else if (o instanceof List) {
        return (List<String>) o;
      } else {
        throw new IllegalArgumentException("Failed to convert " + o + " to List<String>");
      }
    }
  }
}
