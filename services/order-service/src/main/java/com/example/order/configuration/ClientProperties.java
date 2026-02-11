package com.example.order.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.clients")
public record ClientProperties(
    Service inventory
) {
  public record Service(
      String baseUrl,
      int connectTimeoutMs,
      int readTimeoutMs
  ) {}
}

