package com.ramusthastudio.zodiakbot.model;

import com.google.gson.annotations.SerializedName;

public class Result {
  @SerializedName("nama")
  private String name;
  @SerializedName("lahir")
  private String date;
  @SerializedName("usia")
  private String age;
  @SerializedName("zodiak")
  private String zodiac;
  // @SerializedName("ramalan")
  // private Prediction prediction;

  public String getName() { return name; }
  public String getDate() { return date; }
  public String getAge() { return age; }
  public String getZodiac() { return zodiac; }
  // public Prediction getPrediction() { return prediction; }

  public Result setName(String aName) {
    name = aName;
    return this;
  }
  public Result setDate(String aDate) {
    date = aDate;
    return this;
  }
  public Result setAge(String aAge) {
    age = aAge;
    return this;
  }
  public Result setZodiac(String aZodiac) {
    zodiac = aZodiac;
    return this;
  }
  // public Result setPrediction(Prediction aPrediction) {
  //   prediction = aPrediction;
  //   return this;
  // }

  @Override public String toString() {
    return "Result{" +
        "name='" + name + '\'' +
        ", date='" + date + '\'' +
        ", age='" + age + '\'' +
        ", zodiac='" + zodiac + '\'' +
        ", prediction=" +
        '}';
  }
}

