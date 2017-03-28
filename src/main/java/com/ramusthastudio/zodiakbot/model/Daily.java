package com.ramusthastudio.zodiakbot.model;

import com.google.gson.annotations.SerializedName;

public class Daily {
  @SerializedName("umum")
  private String general;
  @SerializedName("percintaan")
  private Romance romance;
  @SerializedName("karir_keuangan")
  private String finance;

  public String getGeneral() { return general; }
  public Romance getRomance() { return romance; }
  public String getFinance() { return finance; }

  public Daily setGeneral(String aGeneral) {
    general = aGeneral;
    return this;
  }
  public Daily setRomance(Romance aRomance) {
    romance = aRomance;
    return this;
  }
  public Daily setFinance(String aFinance) {
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
