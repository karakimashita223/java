package com.monsterloco.repository;

import com.monsterloco.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    List<PriceHistory> findByDrinkIdOrderByTimestampDesc(Long drinkId);

    @Query("SELECT ph FROM PriceHistory ph WHERE ph.drink.id = :drinkId ORDER BY ph.timestamp DESC LIMIT 1")
    Optional<PriceHistory> findLatestPriceByDrinkId(Long drinkId);

    @Query("SELECT ph FROM PriceHistory ph WHERE ph.drink.id = :drinkId ORDER BY ph.timestamp ASC")
    List<PriceHistory> findByDrinkIdOrderByTimestampAsc(Long drinkId);
}
