// Copyright 2016 Twitter. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package com.microsoft.dhalion.metrics;

public class MetricsStats {

  private double metricMax = 0;
  private double metricMin = Double.MAX_VALUE;
  private double metricSum = 0;


  public MetricsStats(double metricMin, double metricMax, double metricSum) {
    this.metricMax = metricMax;
    this.metricMin = metricMin;
    this.metricSum = metricSum;
  }

  public double getMetricSum() {
    return metricSum;
  }

  public double getMetricMax() {
    return metricMax;
  }

  public double getMetricMin() {
    return metricMin;
  }

  public void setMetricSum(double metricSum) {this.metricSum = metricSum;}

  public void setMetricMax(double metricMax) { this.metricMax = metricMax;}

  public void setMetricMin(double metricMin) { this.metricMin = metricMin;}

}
