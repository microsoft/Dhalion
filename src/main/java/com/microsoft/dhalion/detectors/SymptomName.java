package com.microsoft.dhalion.detectors;

public enum SymptomName {
  EXCESS_CPU(ExcessCpuDetector.class.getSimpleName()),
  EXCESS_MEMORY(ExcessMemoryDetector.class.getSimpleName()),
  SCARCE_CPU(ScarceCpuDetector.class.getSimpleName()),
  SCARCE_MEMORY(ScarceMemoryDetector.class.getSimpleName());

  private String text;

  SymptomName(String name) {
    this.text = name;
  }

  public String text() {
    return text;
  }

  @Override
  public String toString() {
    return text();
  }
}
