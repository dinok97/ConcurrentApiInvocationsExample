package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.model.Delay;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {9797, 9898})
public class ConcurrentApiInvocationTest {

  private final ClientAndServer clientAndServer;
  private int mockServerPort;

  public ConcurrentApiInvocationTest(ClientAndServer clientAndServer) {
    this.clientAndServer = clientAndServer;
  }

  @BeforeEach
  void beforeEach() {
    // mock server general config
    ConfigurationProperties.logLevel("ERROR");

    // setup mock server request-response rules
    setupMockServerRequestResponse();
  }

  ApiInvoker apiInvoker = new ApiInvoker();

  @Test
  void invokeApis_sequentially() {
    System.out.println("call multiple APIs synchronously");
    System.out.println("===== start time: " + java.time.LocalTime.now());

    var requests = getHttpRequests();
    for (HttpRequest request : requests) {
      var response = apiInvoker.invokeApi(request).join();
      System.out.println(response.body());
    }

    System.out.println("===== start time: " + java.time.LocalTime.now());
  }

  @Test
  void invokeApis_concurrently() {
    System.out.println("call multiple APIs asynchronously with CompletableFuture");
    System.out.println("===== start time: " + java.time.LocalTime.now());

    var requests = getHttpRequests();
    var responses = apiInvoker.invokeApis(requests).join();
    for (HttpResponse<String> response : responses) {
      System.out.println(response.body());
    }

    System.out.println("===== finish time: " + java.time.LocalTime.now());
  }

  private List<HttpRequest> getHttpRequests() {
    var requests = new ArrayList<HttpRequest>();
    for (int i = 0; i < 3; i++)
      requests.add(
          HttpRequest.newBuilder()
              .uri(URI.create(String.format("http://localhost:%d/api%d", mockServerPort, i)))
              .GET()
              .build()
      );
    return requests;
  }

  private void setupMockServerRequestResponse() {
    for (int i = 0; i < 3; i++) {
      clientAndServer
          .when(
              request()
                  .withMethod("GET")
                  .withPath("/api" + i))
          .respond(
              response()
                  .withStatusCode(200)
                  .withDelay(Delay.milliseconds(1000))
                  .withBody(String.format("{message: 'hello world! from endpoint api%d'}", i))
          );
    }
    mockServerPort = clientAndServer.getPort();
  }
}
