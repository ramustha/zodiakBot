package com.ramusthastudio.zodiakbot.model;

import com.google.gson.annotations.SerializedName;

public class Weekly {
  @SerializedName("umum")
  private Object general;
  @SerializedName("percintaan")
  private Romance romance;
  @SerializedName("karir_keuangan")
  private Object finance;

  public Object getGeneral() { return general; }
  public Romance getRomance() { return romance; }
  public Object getFinance() { return finance; }

  public Weekly setGeneral(Object aGeneral) {
    general = aGeneral;
    return this;
  }
  public Weekly setRomance(Romance aRomance) {
    romance = aRomance;
    return this;
  }
  public Weekly setFinance(Object aFinance) {
    finance = aFinance;
    return this;
  }

  @Override public String toString() {
    return "Weekly{" +
        "general='" + general + '\'' +
        ", romance=" + romance +
        ", finance='" + finance + '\'' +
        '}';
  }
}
