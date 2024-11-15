package com.github.martmatix.tradingapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TradingService {

    private WebClient.Builder builder;

    @Value("${inventory.api.url}")
    private String inventoryHost;

    public WebClient.ResponseSpec sendRequestToInventoryManager(String inventoryId, String newUserId, String authHeader) {
        WebClient webClient = builder.baseUrl(inventoryHost).build();
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/pokemon/inventory/changeOwner")
                        .queryParam("inventory", inventoryId)
                        .queryParam("user", newUserId)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve();
    }

    @Autowired
    public void setBuilder(WebClient.Builder builder) {
        this.builder = builder;
    }
}
