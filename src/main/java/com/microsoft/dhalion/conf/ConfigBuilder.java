package com.microsoft.dhalion.conf;

import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.microsoft.dhalion.HealthManager.CONF_DIR;

public class ConfigBuilder {

  private static final Logger LOG = Logger.getLogger(Config.class.getName());

  private final Map<String, Object> keyValues = new HashMap<>();
  private final Set<PolicyConfig> policyConf = new HashSet<>();
  private final String confDir;

  @Inject
  public ConfigBuilder(@Named(CONF_DIR) String confDir) {
    this.confDir = confDir;
    loadDefaults();
  }

  private void loadDefaults() {
    Arrays.stream(Key.values()).filter(k -> k.getDefault() != null).forEach(k -> put(k, k.getDefault()));
    put(Key.CONF_DIR, confDir);
  }

  public ConfigBuilder loadPolicyConf() {
    Path policyConfigFile = Paths.get(confDir, (String) keyValues.get(Key.POLICY_CONF.value()));
    Map<String, Object> ret = loadFile(policyConfigFile);
    getListOfStrings(ret.get(Key.POLICIES.value()))
        .forEach(id -> policyConf.add(new PolicyConfig(id, (Map<String, Object>) ret.get(id))));
    return this;
  }


  public ConfigBuilder loadConfig(Path file) {
    Map<String, Object> readConfig = loadFile(file);
    return loadConfig(readConfig);
  }

  public ConfigBuilder load(Config ctx) {
    keyValues.putAll(ctx.getCfgMap());
    return this;
  }

  public ConfigBuilder put(String key, Object value) {
    this.keyValues.put(key, value);
    return this;
  }

  public ConfigBuilder put(Key key, Object value) {
    put(key.value(), value);
    return this;
  }

  public ConfigBuilder loadConfig(Map<String, Object> map) {
    keyValues.putAll(map);
    return this;
  }

  public Config build() {
    return new Config(this);
  }

  public Map<String, Object> getKeyValues() {
    return keyValues;
  }

  public Set<PolicyConfig> getPolicyConf() {
    return policyConf;
  }

  private static Map<String, Object> loadFile(Path file) {
    Map<String, Object> props = new HashMap<>();

    // check if the file exists and also it is a regular file
    if (!Files.exists(file) || !Files.isRegularFile(file)) {
      LOG.info("Config file " + file + " does not exist or is not a regular file");
      return props;
    }

    LOG.log(Level.INFO, "Reading config file {0}", file);

    Map<String, Object> propsYaml = null;
    try (FileInputStream fin = new FileInputStream(file.toFile())) {
      Yaml yaml = new Yaml();
      propsYaml = yaml.load(fin);
      LOG.log(Level.INFO, "Successfully read config file {0}", file);
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "Failed to load config file: " + file, e);
    }

    return propsYaml != null ? propsYaml : props;
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