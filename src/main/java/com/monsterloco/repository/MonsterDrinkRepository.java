package com.monsterloco.repository;

import com.monsterloco.model.MonsterDrink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonsterDrinkRepository extends JpaRepository<MonsterDrink, Long> {

    Optional<MonsterDrink> findByProductCode(String productCode);

    @Query("SELECT d FROM MonsterDrink d LEFT JOIN FETCH d.priceHistory ORDER BY d.id")
    List<MonsterDrink> findAllWithPriceHistory();

    @Query("SELECT d FROM MonsterDrink d WHERE d.isMangoLoco = true")
    Optional<MonsterDrink> findMangoLoco();
}
