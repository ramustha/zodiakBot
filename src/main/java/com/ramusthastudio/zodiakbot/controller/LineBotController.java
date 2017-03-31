package com.ramusthastudio.zodiakbot.controller;

import com.google.gson.Gson;
import com.linecorp.bot.client.LineSignatureValidator;
import com.ramusthastudio.zodiakbot.model.Daily;
import com.ramusthastudio.zodiakbot.model.Events;
import com.ramusthastudio.zodiakbot.model.Message;
import com.ramusthastudio.zodiakbot.model.Payload;
import com.ramusthastudio.zodiakbot.model.Postback;
import com.ramusthastudio.zodiakbot.model.Prediction;
import com.ramusthastudio.zodiakbot.model.Result;
import com.ramusthastudio.zodiakbot.model.Source;
import com.ramusthastudio.zodiakbot.model.Weekly;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import retrofit2.Response;

import static com.ramusthastudio.zodiakbot.util.BotHelper.FOLLOW;
import static com.ramusthastudio.zodiakbot.util.BotHelper.JOIN;
import static com.ramusthastudio.zodiakbot.util.BotHelper.KEY_FINANCE;
import static com.ramusthastudio.zodiakbot.util.BotHelper.KEY_GENERAL;
import static com.ramusthastudio.zodiakbot.util.BotHelper.KEY_ROMANCE;
import static com.ramusthastudio.zodiakbot.util.BotHelper.KEY_ZODIAC;
import static com.ramusthastudio.zodiakbot.util.BotHelper.LEAVE;
import static com.ramusthastudio.zodiakbot.util.BotHelper.MESSAGE;
import static com.ramusthastudio.zodiakbot.util.BotHelper.MESSAGE_TEXT;
import static com.ramusthastudio.zodiakbot.util.BotHelper.POSTBACK;
import static com.ramusthastudio.zodiakbot.util.BotHelper.SOURCE_GROUP;
import static com.ramusthastudio.zodiakbot.util.BotHelper.SOURCE_ROOM;
import static com.ramusthastudio.zodiakbot.util.BotHelper.SOURCE_USER;
import static com.ramusthastudio.zodiakbot.util.BotHelper.UNFOLLOW;
import static com.ramusthastudio.zodiakbot.util.BotHelper.generateRandom;
import static com.ramusthastudio.zodiakbot.util.BotHelper.getZodiac;
import static com.ramusthastudio.zodiakbot.util.BotHelper.greetingMessage;
import static com.ramusthastudio.zodiakbot.util.BotHelper.greetingMessageGroup;
import static com.ramusthastudio.zodiakbot.util.BotHelper.instructionTweetsMessage;
import static com.ramusthastudio.zodiakbot.util.BotHelper.pushMessage;
import static com.ramusthastudio.zodiakbot.util.BotHelper.replayMessage;
import static com.ramusthastudio.zodiakbot.util.BotHelper.unfollowMessage;

@RestController
@RequestMapping(value = "/linebot")
public class LineBotController {
  private static final Logger LOG = LoggerFactory.getLogger(LineBotController.class);

  @Autowired
  @Qualifier("line.bot.channelSecret")
  String fChannelSecret;
  @Autowired
  @Qualifier("line.bot.channelToken")
  String fChannelAccessToken;
  @Autowired
  @Qualifier("com.zodiakbot.base_url")
  String fBaseUrl;

  @RequestMapping(value = "/callback", method = RequestMethod.POST)
  public ResponseEntity<String> callback(
      @RequestHeader("X-Line-Signature") String aXLineSignature,
      @RequestBody String aPayload) {

    LOG.info("XLineSignature: {} ", aXLineSignature);
    LOG.info("Payload: {} ", aPayload);

    LOG.info("The Signature is: {} ", (aXLineSignature != null && aXLineSignature.length() > 0) ? aXLineSignature : "N/A");
    final boolean valid = new LineSignatureValidator(fChannelSecret.getBytes()).validateSignature(aPayload.getBytes(), aXLineSignature);
    LOG.info("The Signature is: {} ", valid ? "valid" : "tidak valid");

    LOG.info("Start getting payload ");
    try {

      Gson gson = new Gson();
      Payload payload = gson.fromJson(aPayload, Payload.class);
      Events event = payload.events()[0];

      String eventType = event.type();
      String replayToken = event.replyToken();
      Source source = event.source();
      long timestamp = event.timestamp();
      Message message = event.message();
      Postback postback = event.postback();

      String userId = source.userId();
      String sourceType = source.type();

      LOG.info("source type : {} ", sourceType);
      switch (sourceType) {
        case SOURCE_USER:
          sourceUserProccess(eventType, replayToken, timestamp, message, postback, userId);
          break;
        case SOURCE_GROUP:
          sourceGroupProccess(eventType, replayToken, postback, message, source);
          break;
        case SOURCE_ROOM:
          sourceGroupProccess(eventType, replayToken, postback, message, source);
          break;
      }
    } catch (Exception ae) {
      LOG.error("Erro process payload : {} ", ae.getMessage());
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }
  private void sourceGroupProccess(String aEventType, String aReplayToken, Postback aPostback, Message aMessage, Source aSource) {
    LOG.info("event : {} ", aEventType);
    try {
      switch (aEventType) {
        case LEAVE:
          unfollowMessage(fChannelAccessToken, aSource.groupId());
          break;
        case JOIN:
          LOG.info("Greeting Message");
          greetingMessageGroup(fChannelAccessToken, aSource.groupId());
          break;
        case MESSAGE:
          if (aMessage.type().equals(MESSAGE_TEXT)) {
            String text = aMessage.text();
            replayMessage(fChannelAccessToken, aReplayToken, text);
            if (text.toLowerCase().startsWith(KEY_ZODIAC.toLowerCase())) {
              String candidate = text.substring(KEY_ZODIAC.length(), text.length()).trim();
              generateFullZodiac(aSource.groupId(), candidate, KEY_ZODIAC);
            } else if (text.toLowerCase().startsWith(KEY_GENERAL.toLowerCase())) {
              String candidate = text.substring(KEY_GENERAL.length(), text.length()).trim();
              generateFullZodiac(aSource.groupId(), candidate, KEY_GENERAL);
            } else if (text.toLowerCase().startsWith(KEY_ROMANCE.toLowerCase())) {
              String candidate = text.substring(KEY_ROMANCE.length(), text.length()).trim();
              generateFullZodiac(aSource.groupId(), candidate, KEY_ROMANCE);
            } else if (text.toLowerCase().startsWith(KEY_FINANCE.toLowerCase())) {
              String candidate = text.substring(KEY_FINANCE.length(), text.length()).trim();
              generateFullZodiac(aSource.groupId(), candidate, KEY_FINANCE);
            } else {
              pushMessage(fChannelAccessToken, aSource.groupId(), "Aku gak ngerti nih, " +
                  "aku ini cuma bot yang bisa membaca ramalan zodiak, jadi jangan tanya yang aneh aneh dulu yah");
            }
          } else {
            pushMessage(fChannelAccessToken, aSource.groupId(), "Aku gak ngerti nih, " +
                "aku ini cuma bot yang bisa membaca ramalan zodiak, jadi jangan tanya yang aneh aneh dulu yah");
          }
          break;
        case POSTBACK:
          break;
      }
    } catch (IOException aE) { LOG.error("Message {}", aE.getMessage()); }
  }

  private void sourceUserProccess(String aEventType, String aReplayToken, long aTimestamp, Message aMessage, Postback aPostback, String aUserId) {
    LOG.info("event : {} ", aEventType);
    try {
      switch (aEventType) {
        case UNFOLLOW:
          unfollowMessage(fChannelAccessToken, aUserId);
          break;
        case FOLLOW:
          LOG.info("Greeting Message");
          greetingMessage(fChannelAccessToken, aUserId);
          break;
        case MESSAGE:
          if (aMessage.type().equals(MESSAGE_TEXT)) {
            String text = aMessage.text();
            replayMessage(fChannelAccessToken, aReplayToken, text);
            if (text.toLowerCase().startsWith(KEY_ZODIAC.toLowerCase())) {
              String candidate = text.substring(KEY_ZODIAC.length(), text.length()).trim();
              generateFullZodiac(aUserId, candidate, KEY_ZODIAC);
            } else if (text.toLowerCase().startsWith(KEY_GENERAL.toLowerCase())) {
              String candidate = text.substring(KEY_GENERAL.length(), text.length()).trim();
              generateFullZodiac(aUserId, candidate, KEY_GENERAL);
            } else if (text.toLowerCase().startsWith(KEY_ROMANCE.toLowerCase())) {
              String candidate = text.substring(KEY_ROMANCE.length(), text.length()).trim();
              generateFullZodiac(aUserId, candidate, KEY_ROMANCE);
            } else if (text.toLowerCase().startsWith(KEY_FINANCE.toLowerCase())) {
              String candidate = text.substring(KEY_FINANCE.length(), text.length()).trim();
              generateFullZodiac(aUserId, candidate, KEY_FINANCE);
            } else {
              pushMessage(fChannelAccessToken, aUserId, "Aku gak ngerti nih, " +
                  "aku ini cuma bot yang bisa membaca ramalan zodiak, jadi jangan tanya yang aneh aneh dulu yah");
              instructionTweetsMessage(fChannelAccessToken, aUserId);
            }
          } else {
            pushMessage(fChannelAccessToken, aUserId, "Aku gak ngerti nih, " +
                "aku ini cuma bot yang bisa membaca ramalan zodiak, jadi jangan tanya yang aneh aneh dulu yah");
          }
          break;
        case POSTBACK:
          break;
      }
    } catch (IOException aE) { LOG.error("Message {}", aE.getMessage()); }
  }

  private void generateFullZodiac(String aUserId, String aCandidate, String aFlag) throws IOException {
    String[] candidates = aCandidate.split(" ");
    if (candidates.length == 1) {
      pushMessage(fChannelAccessToken, aUserId, "Kayak nya kamu gak input tanggal nih");
      instructionTweetsMessage(fChannelAccessToken, aUserId);
    } else if (candidates.length > 2) {
      pushMessage(fChannelAccessToken, aUserId, "Aku gak ngerti kamu tulis apa");
      instructionTweetsMessage(fChannelAccessToken, aUserId);
    } else {
      if (candidates[0].trim().length() < 3) {
        pushMessage(fChannelAccessToken, aUserId, "Nama kamu irit banget nih");
      }
      if (candidates[1].trim().length() != 10) {
        pushMessage(fChannelAccessToken, aUserId, "Tanggalnya salah deh kayaknya");
        instructionTweetsMessage(fChannelAccessToken, aUserId);
      } else {
        String name = candidates[0].trim();
        String tgl = candidates[1].trim();
        StringBuilder zodiacBuilder = new StringBuilder();
        LOG.info("Nama {}, tanggal {}", name, tgl);
        Response<Result> zodiac = getZodiac(fBaseUrl, name, tgl);
        LOG.info("zodiac code {} message {}", zodiac.code(), zodiac.message());

        Result result = zodiac.body();
        LOG.info("Name {} {} {} {}", result.getName(), result.getAge(), result.getDate(), result.getZodiac());

        Prediction prediction = result.getPrediction();
        Daily daily = prediction.getDaily();
        Weekly weekly = prediction.getWeekly();
        LOG.info("daily {} {} {}", daily.getGeneral(), daily.getRomance(), daily.getFinance());
        LOG.info("weekly {} {} {}", weekly.getGeneral(), weekly.getRomance(), weekly.getFinance());

        switch (aFlag) {
          case KEY_ZODIAC:
            buildFullDescription(zodiacBuilder, result, daily);
            break;
          case KEY_GENERAL:
            buildGeneralDescription(zodiacBuilder, result, daily);
            break;
          case KEY_FINANCE:
            buildFinanceDescription(zodiacBuilder, result, daily);
            break;
          case KEY_ROMANCE:
            buildRomanceDescription(zodiacBuilder, result, daily);
            break;
        }

        pushMessage(fChannelAccessToken, aUserId, zodiacBuilder.toString());
        generateRandomMessage(aUserId, aFlag);
      }
    }
  }

  private static void buildFullDescription(StringBuilder aZodiacBuilder, Result aResult, Daily aDaily) {
    aZodiacBuilder
        .append("Nama : ").append(aResult.getName()).append("\n")
        .append("Lahir : ").append(aResult.getDate()).append("\n")
        .append("Usia : ").append(aResult.getAge()).append("\n")
        .append("Zodiak : ").append(aResult.getZodiac()).append("\n\n")

        .append("Hasil Ramalan kamu").append("\n\n")
        .append("Umum : ").append("\n").append(aDaily.getGeneral()).append("\n\n")

        .append("Buat single : ").append("\n").append(aDaily.getRomance().getSingle()).append("\n")
        .append("Buat couple : ").append("\n").append(aDaily.getRomance().getCouple()).append("\n\n")

        .append("Keuangan : ").append("\n").append(aDaily.getFinance());
  }

  private static void buildGeneralDescription(StringBuilder aZodiacBuilder, Result aResult, Daily aDaily) {
    aZodiacBuilder
        .append("Nama : ").append(aResult.getName()).append("\n")
        .append("Lahir : ").append(aResult.getDate()).append("\n")
        .append("Usia : ").append(aResult.getAge()).append("\n")
        .append("Zodiak : ").append(aResult.getZodiac()).append("\n\n")

        .append("Umum : ").append("\n").append(aDaily.getGeneral());
  }

  private static void buildRomanceDescription(StringBuilder aZodiacBuilder, Result aResult, Daily aDaily) {
    aZodiacBuilder
        .append("Nama : ").append(aResult.getName()).append("\n")
        .append("Lahir : ").append(aResult.getDate()).append("\n")
        .append("Usia : ").append(aResult.getAge()).append("\n")
        .append("Zodiak : ").append(aResult.getZodiac()).append("\n\n")

        .append("Buat single : ").append("\n").append(aDaily.getRomance().getSingle()).append("\n")
        .append("Buat couple : ").append("\n").append(aDaily.getRomance().getCouple());
  }

  private static void buildFinanceDescription(StringBuilder aZodiacBuilder, Result aResult, Daily aDaily) {
    aZodiacBuilder
        .append("Nama : ").append(aResult.getName()).append("\n")
        .append("Lahir : ").append(aResult.getDate()).append("\n")
        .append("Usia : ").append(aResult.getAge()).append("\n")
        .append("Zodiak : ").append(aResult.getZodiac()).append("\n\n")

        .append("Keuangan : ").append("\n").append(aDaily.getFinance());
  }

  private void generateRandomMessage(String aUserId, String aFlag) throws IOException {
    if (generateRandom(0, 5) > 2) {
      pushMessage(fChannelAccessToken, aUserId, "Kalau kamu suka sama aku, tolong donk bantu invite supaya aku banyak teman, ini id aku @yjb9380i");
    }
    switch (aFlag) {
      case KEY_ZODIAC:
        if (generateRandom(0, 5) > 2) {
          pushMessage(fChannelAccessToken, aUserId, "Kamu pengen ramalan secara umum tulis aja umum reza 20-05-1990");
        } else if (generateRandom(5, 10) > 5) {
          pushMessage(fChannelAccessToken, aUserId, "Kamu pengen ramalan percintaan tulis aja percintaan reza 20-05-1990");
        } else {
          pushMessage(fChannelAccessToken, aUserId, "Kamu pengen ramalan keuangan tulis aja keuangan reza 20-05-1990");
        }
        break;
      case KEY_GENERAL:
        if (generateRandom(0, 5) > 2) {
          pushMessage(fChannelAccessToken, aUserId, "Kamu pengen ramalan percintaan tulis aja percintaan reza 20-05-1990");
        }
        break;
      case KEY_FINANCE:
        if (generateRandom(0, 5) > 2) {
          pushMessage(fChannelAccessToken, aUserId, "Kamu pengen ramalan secara umum tulis aja umum reza 20-05-1990");
        }
        break;
      case KEY_ROMANCE:
        if (generateRandom(0, 5) > 2) {
          pushMessage(fChannelAccessToken, aUserId, "Kamu pengen ramalan secara umum tulis aja umum reza 20-05-1990");
        }
        break;
    }
  }

}
