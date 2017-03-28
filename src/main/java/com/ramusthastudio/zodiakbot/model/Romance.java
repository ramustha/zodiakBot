package com.ramusthastudio.zodiakbot.model;

import com.google.gson.annotations.SerializedName;

public class Romance {
  @SerializedName("single")
  private Object single;
  @SerializedName("couple")
  private Object couple;

  public Object getSingle() { return single; }
  public Object getCouple() { return couple; }

  public Romance setSingle(Object aSingle) {
    single = aSingle;
    return this;
  }
  public Romance setCouple(Object aCouple) {
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
