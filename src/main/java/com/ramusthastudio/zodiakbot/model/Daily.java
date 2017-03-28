package com.ramusthastudio.zodiakbot.model;

import com.google.gson.annotations.SerializedName;

public class Daily {
  @SerializedName("umum")
  private Object general;
  @SerializedName("percintaan")
  private Romance romance;
  @SerializedName("karir_keuangan")
  private Object finance;

  public Object getGeneral() { return general; }
  public Romance getRomance() { return romance; }
  public Object getFinance() { return finance; }

  public Daily setGeneral(Object aGeneral) {
    general = aGeneral;
    return this;
  }
  public Daily setRomance(Romance aRomance) {
    romance = aRomance;
    return this;
  }
  public Daily setFinance(Object aFinance) {
    finance = aFinance;
    return this;
  }

  @Override public String toString() {
    return "Daily{" +
        "general='" + general + '\'' +
        ", romance=" + romance +
        ", finance='" + finance + '\'' +
        '}';
  }
}
