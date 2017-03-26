package com.ramusthastudio.zodiakbot.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class Config {
  @Bean(name = "line.bot.channelSecret")
  public String getChannelSecret() { return System.getenv("line.bot.channelSecret"); }
  @Bean(name = "line.bot.channelToken")
  public String getChannelAccessToken() { return System.getenv("line.bot.channelToken"); }
}
