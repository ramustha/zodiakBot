package com.ramusthastudio.zodiakbot.model;

import com.google.gson.annotations.SerializedName;

public class Prediction {
  @SerializedName("harian")
  private Daily daily;
  @SerializedName("mingguan")
  private Weekly weekly;

  public Daily getDaily() { return daily; }
  public Weekly getWeekly() { return weekly; }

  public Prediction setDaily(Daily aDaily) {
    daily = aDaily;
    return this;
  }
  public Prediction setWeekly(Weekly aWeekly) {
    weekly = aWeekly;
    return this;
  }

  @Override public String toString() {
    return "Prediction{" +
        "daily=" + daily +
        ", weekly=" + weekly +
        '}';
  }
}
