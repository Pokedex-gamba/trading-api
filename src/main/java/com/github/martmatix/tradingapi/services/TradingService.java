package com.github.martmatix.tradingapi.services;

import com.github.martmatix.tradingapi.entities.TradeEntity;
import com.github.martmatix.tradingapi.repositories.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class TradingService {

    private WebClient.Builder builder;

    @Value("${inventory.api.url}")
    private String inventoryHost;

    private TradeRepository tradeRepository;

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

    public void saveTrade(TradeEntity trade) {
        tradeRepository.save(trade);
    }

    public List<TradeEntity> findAllUserTrades(String userId) {
        return tradeRepository.findAllByUserId(userId);
    }

    @Autowired
    public void setBuilder(WebClient.Builder builder) {
        this.builder = builder;
    }

    @Autowired
    public void setTradeRepository(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }
}
