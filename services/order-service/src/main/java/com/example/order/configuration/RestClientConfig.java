package com.example.order.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean
  RestClient inventoryClient(RestClient.Builder builder, ClientProperties props) {
    return build(builder, props.inventory());
  }

  private RestClient build(RestClient.Builder builder, ClientProperties.Service svc) {
    var rf = new SimpleClientHttpRequestFactory();
    rf.setConnectTimeout(svc.connectTimeoutMs());
    rf.setReadTimeout(svc.readTimeoutMs());

    return builder
        .baseUrl(svc.baseUrl())
        .requestFactory(rf)
        .build();
  }
}

