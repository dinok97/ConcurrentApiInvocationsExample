package org.example;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

public class ApiInvoker {
  HttpClient client = HttpUtils.newHttpClient();

  public CompletableFuture<HttpResponse<String>> invokeApi(HttpRequest request) {
    return sendRequest(request);
  }

  public CompletableFuture<List<HttpResponse<String>>> invokeApis(List<HttpRequest> requests) {
    var responseFutures = requests.stream()
        .map(this::sendRequest)
        .collect(toList());
    var resultList = responseFutures.stream()
            .collect(CompletableFutureCollector.collectAndTransform());
    return resultList;
  }

  private CompletableFuture<HttpResponse<String>> sendRequest(HttpRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
        throw new RuntimeException("Something wrong with your request");
      }
    });
  }
}
