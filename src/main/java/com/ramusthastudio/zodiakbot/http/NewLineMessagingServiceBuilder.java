package com.ramusthastudio.zodiakbot.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.linecorp.bot.client.LineMessagingService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class NewLineMessagingServiceBuilder {
  public static final String DEFAULT_API_END_POINT = "https://api.line.me/";
  public static final long DEFAULT_CONNECT_TIMEOUT = 10_000;
  public static final long DEFAULT_READ_TIMEOUT = 10_000;
  public static final long DEFAULT_WRITE_TIMEOUT = 10_000;

  private String apiEndPoint = DEFAULT_API_END_POINT;
  private long connectTimeout = DEFAULT_CONNECT_TIMEOUT;
  private long readTimeout = DEFAULT_READ_TIMEOUT;
  private long writeTimeout = DEFAULT_WRITE_TIMEOUT;
  private List<Interceptor> interceptors = new ArrayList<>();

  private OkHttpClient.Builder okHttpClientBuilder;
  private Retrofit.Builder retrofitBuilder;

  /**
   * Create a new {@link NewLineMessagingServiceBuilder} with specified channelToken.
   */
  public static NewLineMessagingServiceBuilder create(String channelToken) {
    return new NewLineMessagingServiceBuilder(defaultInterceptors(channelToken));
  }

  private NewLineMessagingServiceBuilder(List<Interceptor> interceptors) {
    this.interceptors.addAll(interceptors);
  }

  /**
   * Set apiEndPoint.
   */
  public NewLineMessagingServiceBuilder apiEndPoint(String apiEndPoint) {
    this.apiEndPoint = apiEndPoint;
    return this;
  }

  /**
   * Set connectTimeout in milliseconds.
   */
  public NewLineMessagingServiceBuilder connectTimeout(long connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  /**
   * Set readTimeout in milliseconds.
   */
  public NewLineMessagingServiceBuilder readTimeout(long readTimeout) {
    this.readTimeout = readTimeout;
    return this;
  }

  /**
   * Set writeTimeout in milliseconds.
   */
  public NewLineMessagingServiceBuilder writeTimeout(long writeTimeout) {
    this.writeTimeout = writeTimeout;
    return this;
  }

  /**
   * Add interceptor
   */
  public NewLineMessagingServiceBuilder addInterceptor(Interceptor interceptor) {
    this.interceptors.add(interceptor);
    return this;
  }

  /**
   * Add interceptor first
   */
  public NewLineMessagingServiceBuilder addInterceptorFirst(Interceptor interceptor) {
    this.interceptors.add(0, interceptor);
    return this;
  }

  /**
   * <p>If you want to use your own setting, specify {@link OkHttpClient.Builder} instance.</p>
   */
  public NewLineMessagingServiceBuilder okHttpClientBuilder(OkHttpClient.Builder okHttpClientBuilder) {
    this.okHttpClientBuilder = okHttpClientBuilder;
    return this;
  }

  /**
   * <p>If you want to use your own setting, specify {@link Retrofit.Builder} instance.</p>
   *
   * <p>ref: {@link NewLineMessagingServiceBuilder#createDefaultRetrofitBuilder()} ()}.</p>
   */
  public NewLineMessagingServiceBuilder retrofitBuilder(Retrofit.Builder retrofitBuilder) {
    this.retrofitBuilder = retrofitBuilder;
    return this;
  }

  /**
   * Creates a new {@link LineMessagingService}.
   */
  public LineMessagingService build() {
    if (okHttpClientBuilder == null) {
      okHttpClientBuilder = new OkHttpClient.Builder();
      interceptors.forEach(okHttpClientBuilder::addInterceptor);
    }
    okHttpClientBuilder
        .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
        .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
        .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS);
    final OkHttpClient okHttpClient = okHttpClientBuilder.build();

    if (retrofitBuilder == null) {
      retrofitBuilder = createDefaultRetrofitBuilder();
    }
    // retrofitBuilder.client(okHttpClient);
    retrofitBuilder.baseUrl(apiEndPoint);
    final Retrofit retrofit = retrofitBuilder.build();

    return retrofit.create(LineMessagingService.class);
  }

  private static List<Interceptor> defaultInterceptors(String channelToken) {
    final Logger slf4jLogger = LoggerFactory.getLogger("com.linecorp.bot.client.wire");
    final HttpLoggingInterceptor httpLoggingInterceptor =
        new HttpLoggingInterceptor(message -> slf4jLogger.info("{}", message));
    httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

    return Arrays.asList(
        new NewHeaderInterceptor(channelToken),
        httpLoggingInterceptor
    );
  }

  private static Retrofit.Builder createDefaultRetrofitBuilder() {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Register JSR-310(java.time.temporal.*) module and read number as millsec.
    objectMapper.registerModule(new JavaTimeModule())
        .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

    return new Retrofit.Builder()
        .addConverterFactory(JacksonConverterFactory.create(objectMapper));
  }
}
