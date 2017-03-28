package com.ramusthastudio.zodiakbot.controller;

import com.ramusthastudio.zodiakbot.model.Result;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ZodiakService {

  @GET("/zodiak")
  Call<Result> zodiac(@Query("nama") String aNama, @Query("tgl") String aDate);
}
