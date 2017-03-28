package com.ramusthastudio.zodiakbot.model;

import com.google.gson.annotations.SerializedName;

public class Result {
  @SerializedName("nama")
  private Object name;
  @SerializedName("lahir")
  private Object date;
  @SerializedName("usia")
  private Object age;
  @SerializedName("zodiak")
  private Object zodiac;
  @SerializedName("ramalan")
  private Prediction prediction;

  public Object getName() { return name; }
  public Object getDate() { return date; }
  public Object getAge() { return age; }
  public Object getZodiac() { return zodiac; }
  public Prediction getPrediction() { return prediction; }

  public Result setName(Object aName) {
    name = aName;
    return this;
  }
  public Result setDate(Object aDate) {
    date = aDate;
    return this;
  }
  public Result setAge(Object aAge) {
    age = aAge;
    return this;
  }
  public Result setZodiac(Object aZodiac) {
    zodiac = aZodiac;
    return this;
  }
  public Result setPrediction(Prediction aPrediction) {
    prediction = aPrediction;
    return this;
  }

  @Override public String toString() {
    return "Result{" +
        "name='" + name + '\'' +
        ", date='" + date + '\'' +
        ", age='" + age + '\'' +
        ", zodiac='" + zodiac + '\'' +
        ", prediction=" + prediction +
        '}';
  }
}

