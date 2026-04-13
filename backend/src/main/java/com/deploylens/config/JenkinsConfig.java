package com.deploylens.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.Base64;

@Configuration
public class JenkinsConfig {

    @Value("${jenkins.base-url}")
    private String baseUrl;

    @Value("${jenkins.username}")
    private String username;

    @Value("${jenkins.api-token}")
    private String apiToken;

    @Bean
    public RestTemplate jenkinsRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getInterceptors().add((request, body, execution) -> {
            String credentials = username + ":" + apiToken;
            String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
            request.getHeaders().add("Authorization", "Basic " + encoded);
            return execution.execute(request, body);
        });
        return restTemplate;
    }

    public String getBaseUrl() { return baseUrl; }
    public String getUsername() { return username; }
}
