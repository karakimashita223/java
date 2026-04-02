package com.monsterloco.repository;

import com.monsterloco.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findAllByOrderByCreatedAtDesc();

    Optional<CartItem> findByDrinkId(Long drinkId);

    boolean existsByDrinkId(Long drinkId);

    void deleteByDrinkId(Long drinkId);
}
