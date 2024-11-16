package com.github.martmatix.tradingapi.repositories;

import com.github.martmatix.tradingapi.entities.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, UUID> {

    List<TradeEntity> findAllByUserId(String userId);

}
