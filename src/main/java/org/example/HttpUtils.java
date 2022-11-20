package org.example;

import java.net.http.HttpClient;
import java.time.Duration;

public class HttpUtils {
  public static HttpClient newHttpClient() {
    return HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(10))
        .build();
  }
}
