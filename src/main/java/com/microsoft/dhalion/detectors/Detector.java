package com.microsoft.dhalion.detectors;

import com.microsoft.dhalion.api.IDetector;
import com.microsoft.dhalion.policy.PoliciesExecutor.ExecutionContext;

public class Detector implements IDetector {

  protected ExecutionContext context;

  @Override
  public void initialize(ExecutionContext context) {
    this.context = context;
  }

}
