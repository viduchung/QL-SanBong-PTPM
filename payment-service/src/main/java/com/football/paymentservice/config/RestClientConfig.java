package com.football.paymentservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${booking.service.base-url:http://localhost:8081}")
    private String bookingServiceBaseUrl;

    @Value("${booking.service.connect-timeout:2000}")
    private int connectTimeout;

    @Value("${booking.service.read-timeout:3000}")
    private int readTimeout;

    @Bean
    public RestClient bookingRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        factory.setReadTimeout(Duration.ofMillis(readTimeout));

        return RestClient.builder()
                .baseUrl(bookingServiceBaseUrl)
                .requestFactory(factory)
                .build();
    }
}
