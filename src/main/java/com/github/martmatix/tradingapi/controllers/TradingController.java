package com.github.martmatix.tradingapi.controllers;

import com.github.martmatix.tradingapi.constants.ErrorCodes;
import com.github.martmatix.tradingapi.entities.TradeEntity;
import com.github.martmatix.tradingapi.services.KeyLoaderService;
import com.github.martmatix.tradingapi.services.TradingService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.PublicKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class TradingController {

    private KeyLoaderService keyLoaderService;
    private TradingService tradingService;

    @GetMapping(path = "/pokemon/trading/tradePokemon")
    public ResponseEntity<?> tradePokemon(@RequestParam("inventory") String inventoryId, @RequestParam("user") String newUserId, @RequestHeader("Authorization") String authHeader) {
        try {
            if (inventoryId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"Inventory ID Is Missing In Request\"}");
            }

            if (newUserId.trim().isEmpty() || newUserId.trim().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"User ID Is Missing In Request\"}");
            }

            String userId = getUserIdFromToken(authHeader);
            if (userId.equals(ErrorCodes.TOKEN_EXTRACTION_ERROR.getCode())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Unable To Process Request: " + ErrorCodes.TOKEN_EXTRACTION_ERROR.getCode() + "\"}");
            }
            if (userId.equals(ErrorCodes.PUBLIC_NOT_FOUND.getCode())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Unable To Process Request: " + ErrorCodes.PUBLIC_NOT_FOUND.getCode() + "\"}");
            }

            String responseMessage = tradingService.sendRequestToInventoryManager(inventoryId, newUserId, authHeader).bodyToMono(String.class).block();

            TradeEntity tradeEntity = new TradeEntity();
            tradeEntity.setDate(new Date());
            tradeEntity.setUserId(userId);
            tradeEntity.setNewUserId(newUserId);
            tradeEntity.setInventoryId(inventoryId);
            tradingService.saveTrade(tradeEntity);

            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping(path = "/pokemon/trading/tradeHistory")
    public ResponseEntity<?> tradeHistory(@RequestHeader("Authorization") String authHeader) {
        String userId = getUserIdFromToken(authHeader);
        if (userId.equals(ErrorCodes.TOKEN_EXTRACTION_ERROR.getCode())) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Unable To Process Request: " + ErrorCodes.TOKEN_EXTRACTION_ERROR.getCode() + "\"}");
        }
        if (userId.equals(ErrorCodes.PUBLIC_NOT_FOUND.getCode())) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Unable To Process Request: " + ErrorCodes.PUBLIC_NOT_FOUND.getCode() + "\"}");
        }

        List<TradeEntity> allUserTrades = tradingService.findAllUserTrades(userId);
        return ResponseEntity.ok(allUserTrades);
    }

    private String getUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer", "").trim();

        PublicKey publicKey;
        try {
            String path = TradingController.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File publicKeyFile = new File(path, "decoding_key");
            if (!publicKeyFile.exists()) {
                return ErrorCodes.PUBLIC_NOT_FOUND.getCode();
            }
            BufferedReader reader = new BufferedReader(new FileReader(publicKeyFile));
            String publicKeyContent = reader.lines().collect(Collectors.joining("\n"));
            reader.close();
            publicKey = keyLoaderService.getPublicKey(publicKeyContent);
        } catch (Exception e) {
            return ErrorCodes.TOKEN_EXTRACTION_ERROR.getCode();
        }

        Claims claims = Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();

        String userId = claims.get("user_id", String.class);
        if (userId == null) {
            return ErrorCodes.TOKEN_EXTRACTION_ERROR.getCode();
        }

        return userId;
    }

    @Autowired
    public void setKeyLoaderService(KeyLoaderService keyLoaderService) {
        this.keyLoaderService = keyLoaderService;
    }

    @Autowired
    public void setTradingService(TradingService tradingService) {
        this.tradingService = tradingService;
    }
}
