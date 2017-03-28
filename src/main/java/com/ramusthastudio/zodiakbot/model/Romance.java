package com.ramusthastudio.zodiakbot.model;

import com.google.gson.annotations.SerializedName;

public class Romance {
  @SerializedName("single")
  private String single;
  @SerializedName("couple")
  private String couple;

  public String getSingle() { return single; }
  public String getCouple() { return couple; }

  public Romance setSingle(String aSingle) {
    single = aSingle;
    return this;
  }
  public Romance setCouple(String aCouple) {
    couple = aCouple;
    return this;
  }

  @Override public String toString() {
    return "Romance{" +
        "single='" + single + '\'' +
        ", couple='" + couple + '\'' +
        '}';
  }
}
