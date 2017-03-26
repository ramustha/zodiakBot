package com.ramusthastudio.zodiakbot.util;

import com.linecorp.bot.client.LineMessagingService;
import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.model.Multicast;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import com.linecorp.bot.model.message.template.Template;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import java.io.IOException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;
import twitter4j.User;

import static com.ramusthastudio.zodiakbot.util.StickerHelper.JAMES_STICKER_TWO_THUMBS;

public final class BotHelper {
  private static final Logger LOG = LoggerFactory.getLogger(BotHelper.class);

  public static final String SOURCE_USER = "user";
  public static final String SOURCE_GROUP = "group";
  public static final String SOURCE_ROOM = "room";

  public static final String JOIN = "join";
  public static final String FOLLOW = "follow";
  public static final String UNFOLLOW = "unfollow";
  public static final String MESSAGE = "message";
  public static final String LEAVE = "leave";
  public static final String POSTBACK = "postback";
  public static final String BEACON = "beacon";

  public static final String MESSAGE_TEXT = "text";
  public static final String MESSAGE_IMAGE = "image";
  public static final String MESSAGE_VIDEO = "video";
  public static final String MESSAGE_AUDIO = "audio";
  public static final String MESSAGE_LOCATION = "location";
  public static final String MESSAGE_STICKER = "sticker";

  public static final String KEY_TWITTER = "twitter";
  public static final String KEY_PERSONALITY = "personality";
  public static final String KEY_SUMMARY = "summary";
  public static final String KEY_POSITIVE = "twitter positif";
  public static final String KEY_NEGATIVE = "twitter negatif";
  public static final String KEY_FRIEND = "teman";
  public static final String KEY_AMA = "ama";

  private static LineMessagingService lineServiceBuilder(String aChannelAccessToken) {
    OkHttpClient.Builder client = new OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .cache(null)
        .connectTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS);

    LOG.info("Starting line messaging service to 1.0...");
    return LineMessagingServiceBuilder
        .create(aChannelAccessToken)
        .okHttpClientBuilder(enableTls12(client))
        .build();
  }

  public static OkHttpClient.Builder enableTls12(OkHttpClient.Builder client) {
    try {
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
          TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init((KeyStore) null);
      TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
      if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
        throw new IllegalStateException("Unexpected default trust managers:"
            + Arrays.toString(trustManagers));
      }
      X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

      SSLContext sslContext = SSLContext.getInstance("TLSv1");
      sslContext.init(null, new TrustManager[] {trustManager}, null);
      SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
      client.sslSocketFactory(sslSocketFactory, trustManager);

      ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
          .allEnabledCipherSuites()
          .allEnabledTlsVersions()
          .build();

      List<ConnectionSpec> specs = new ArrayList<>();
      specs.add(cs);
      specs.add(ConnectionSpec.COMPATIBLE_TLS);
      specs.add(ConnectionSpec.CLEARTEXT);

      client.connectionSpecs(specs);
    } catch (Exception exc) {
      LOG.error("Error while setting TLS 1.0 {}", exc.getMessage());
    }
    return client;
  }

  public static UserProfileResponse getUserProfile(String aChannelAccessToken,
      String aUserId) throws IOException {
    LOG.info("getUserProfile...");
    return lineServiceBuilder(aChannelAccessToken).getProfile(aUserId).execute().body();
  }

  public static Response<BotApiResponse> replayMessage(String aChannelAccessToken, String aReplayToken,
      String aMsg) throws IOException {
    TextMessage message = new TextMessage(aMsg);
    ReplyMessage pushMessage = new ReplyMessage(aReplayToken, message);
    LOG.info("replayMessage...");
    return lineServiceBuilder(aChannelAccessToken).replyMessage(pushMessage).execute();
  }

  public static Response<BotApiResponse> pushMessage(String aChannelAccessToken, String aUserId,
      String aMsg) throws IOException {
    TextMessage message = new TextMessage(aMsg);
    PushMessage pushMessage = new PushMessage(aUserId, message);
    LOG.info("pushMessage...");
    return lineServiceBuilder(aChannelAccessToken).pushMessage(pushMessage).execute();
  }

  public static Response<BotApiResponse> multicastMessage(String aChannelAccessToken, Set<String> aUserIds,
      String aMsg) throws IOException {
    TextMessage message = new TextMessage(aMsg);
    Multicast pushMessage = new Multicast(aUserIds, message);
    LOG.info("multicastMessage...");
    return lineServiceBuilder(aChannelAccessToken).multicast(pushMessage).execute();
  }

  public static Response<BotApiResponse> templateMessage(String aChannelAccessToken, String aUserId,
      Template aTemplate) throws IOException {
    TemplateMessage message = new TemplateMessage("Result", aTemplate);
    PushMessage pushMessage = new PushMessage(aUserId, message);
    LOG.info("templateMessage...");
    return lineServiceBuilder(aChannelAccessToken).pushMessage(pushMessage).execute();
  }

  public static Response<BotApiResponse> stickerMessage(String aChannelAccessToken, String aUserId,
      StickerHelper.StickerMsg aSt) throws IOException {
    StickerMessage message = new StickerMessage(aSt.pkgId(), aSt.id());
    PushMessage pushMessage = new PushMessage(aUserId, message);
    LOG.info("stickerMessage...");
    return lineServiceBuilder(aChannelAccessToken).pushMessage(pushMessage).execute();
  }

  public static Response<BotApiResponse> profileUserMessage(String aChannelAccessToken, String aUserId, User aUser) throws IOException {
    String title = aUser.getName();
    title = title.length() > 39 ? title.substring(0, 34) + "..." : title;

    String desc = aUser.getDescription();
    if (aUser.getDescription().isEmpty()) {
      desc = "Gak nyantumin deskripsi";
    } else {
      desc = desc.length() > 59 ? desc.substring(0, 54) + "..." : desc;
    }

    LOG.info("profileUserMessage {} {} {} {} ", aUser.getOriginalProfileImageURLHttps(), title, desc, aUser.getScreenName());
    ButtonsTemplate template = new ButtonsTemplate(
        aUser.getOriginalProfileImageURLHttps(),
        title,
        desc,
        Arrays.asList(
            new PostbackAction("Sentiment", KEY_TWITTER + " " + aUser.getScreenName()),
            new PostbackAction("Personality", KEY_PERSONALITY + " " + aUser.getScreenName()),
            new PostbackAction("Summary", KEY_SUMMARY + " " + aUser.getScreenName()
            )
        ));

    return templateMessage(aChannelAccessToken, aUserId, template);
  }
  public static void talkMessageGroup(String aChannelAccessToken, String aUserId) throws IOException {
    String greeting = "Hi\n";
    greeting += "Kenalin, aku AMA bot yang bisa membaca sentiment lewat twitter, ";
    greeting += "sentiment atau pendapat orang tentang apapun di dalam dunia twitter, ";
    greeting += "selain sentiment aku juga bisa baca personality nya lho.\n\n";
    greeting += "Personality menggambarkan karakter seseorang dari sebuah tulisan atau pun sosial media, ";
    greeting += "saat ini aku hanya bisa membaca karakter seseorang lewat tweets ataupun tulisan dalam sebuah file.";
    stickerMessage(aChannelAccessToken, aUserId, new StickerHelper.StickerMsg(JAMES_STICKER_TWO_THUMBS));
    pushMessage(aChannelAccessToken, aUserId, greeting);
  }

  public static void greetingMessageGroup(String aChannelAccessToken, String aUserId) throws IOException {
    String greeting = "Hi manteman\n";
    greeting += "Makasih aku udah di invite disini!\n";
    greeting += "sentiment atau pendapat orang tentang apapun di dalam dunia twitter, ";
    greeting += "selain sentiment aku juga bisa baca personality nya lho.\n\n";
    greeting += "Personality menggambarkan karakter seseorang dari sebuah tulisan atau pun sosial media, ";
    greeting += "saat ini aku hanya bisa membaca karakter seseorang lewat tweets ataupun tulisan dalam sebuah file.\n\n";
    greeting += "Bantuin aku donk supaya punya banyak teman, ini id aku @ape3119w";
    stickerMessage(aChannelAccessToken, aUserId, new StickerHelper.StickerMsg(JAMES_STICKER_TWO_THUMBS));
    pushMessage(aChannelAccessToken, aUserId, greeting);
  }

  public static void greetingMessage(String aChannelAccessToken, String aUserId) throws IOException {
    UserProfileResponse userProfile = getUserProfile(aChannelAccessToken, aUserId);
    String greeting = "Hi " + userProfile.getDisplayName() + "\n";
    greeting += "Makasih udah nambahin aku sebagai teman!\n";
    greeting += "Kenalin, aku AMA bot yang bisa membaca sentiment lewat twitter, ";
    greeting += "sentiment atau pendapat orang tentang apapun di dalam dunia twitter, ";
    greeting += "selain sentiment aku juga bisa baca personality nya lho.\n\n";
    greeting += "Personality menggambarkan karakter seseorang dari sebuah tulisan atau pun sosial media, ";
    greeting += "saat ini aku hanya bisa membaca karakter seseorang lewat tweets ataupun tulisan dalam sebuah file.\n\n";
    greeting += "Bantuin aku donk supaya punya banyak teman, ini id aku @ape3119w";
    stickerMessage(aChannelAccessToken, aUserId, new StickerHelper.StickerMsg(JAMES_STICKER_TWO_THUMBS));
    pushMessage(aChannelAccessToken, aUserId, greeting);
  }

  public static void unfollowMessage(String aChannelAccessToken, String aUserId) throws IOException {
    UserProfileResponse userProfile = getUserProfile(aChannelAccessToken, aUserId);
    String greeting = "Hi " + userProfile.getDisplayName() + "\n";
    greeting += "Kenapa kamu unfollow aku? jahat !!!";
    pushMessage(aChannelAccessToken, aUserId, greeting);
  }

  public static void instructionSentimentMessage(String aChannelAccessToken, String aUserId) throws IOException {
    String greeting = "Contoh kalau kamu pengen tau nih pendapat orang lain tentang indonesia, ";
    greeting += "Kamu tinggal tulis sentiment 'indonesia', ";
    greeting += "kalau pengen tau tentang karakter seseorang tulis personality 'jokowi' atau ";
    greeting += "summary 'jokowi' buat tau pribadi 'jokowi' lebih dalam seperti apa yang 'jokowi' suka dan gak suka, ";
    greeting += "nanti aku kumpulin infonya terus aku kasih tau ke kamu.\n\n";
    greeting += "Kalau kamu pengen tau tentang karakter seseorang dari tulisan seseorang kamu tinggal kirim file tulisanya ke sini.\n\n";
    greeting += "Saat ini aku cuma bisa baca tulisan pake bahasa inggris aja, untuk bahasa lain aku belum bisa.";
    pushMessage(aChannelAccessToken, aUserId, greeting);
  }

  public static void instructionTweetsMessage(String aChannelAccessToken, String aUserId) throws IOException {
    UserProfileResponse userProfile = getUserProfile(aChannelAccessToken, aUserId);
    String greeting = "Hi " + userProfile.getDisplayName() + "\n";
    greeting += "Aku juga bisa nih lihat profile twitter orang, kamu tinggal tulis aja id twitternya\n";
    greeting += "Contoh twitter 'dicoding'";
    pushMessage(aChannelAccessToken, aUserId, greeting);
  }

  public static Response<BotApiResponse> confirmTwitterMessage(String aChannelAccessToken, String aUserId, String aMsg, String aDataYes, String aDataNo) throws IOException {
    ConfirmTemplate template = new ConfirmTemplate(aMsg, Arrays.asList(
        new PostbackAction("Bener", aDataYes),
        new PostbackAction("Salah", aDataNo)
    ));
    return templateMessage(aChannelAccessToken, aUserId, template);
  }

  public static int generateRandom(int min, int max) {
    Random r = new Random();
    return r.nextInt(max - min) + min;
  }

  public static String predictWord(String aText, String aFind) {
    Pattern word = Pattern.compile(aFind);
    Matcher match = word.matcher(aText);
    String result = "";
    while (match.find()) {
      String predictAfterKey = removeAnySymbol(aText.substring(match.end(), aText.length())).trim();

      if (predictAfterKey.length() > 0) {
        if (predictAfterKey.contains(" ")) {
          String[] predictAfterKeySplit = predictAfterKey.split(" ");
          result = predictAfterKeySplit[0];
        } else {
          result = predictAfterKey;
        }
        return result;
      }
    }
    return result;
  }

  public static String removeAnySymbol(String s) {
    Pattern pattern = Pattern.compile("[^a-z A-Z^0-9]");
    Matcher matcher = pattern.matcher(s);
    return matcher.replaceAll(" ");
  }
}
