package com.banquito.switchpagos.integrationcore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CoreBancarioRestClientConfig {

    @Bean
    public RestClient coreBancarioRestClient(CoreBancarioProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
