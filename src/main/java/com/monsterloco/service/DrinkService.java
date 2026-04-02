package com.monsterloco.service;

import com.monsterloco.model.MonsterDrink;
import com.monsterloco.model.PriceHistory;
import com.monsterloco.repository.MonsterDrinkRepository;
import com.monsterloco.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DrinkService {

    private final MonsterDrinkRepository drinkRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final ScraperService scraperService;

    public List<MonsterDrink> getAllDrinks() {
        return drinkRepository.findAll();
    }

    public List<DrinkWithPrice> getAllDrinksWithLatestPrice() {
        List<MonsterDrink> drinks = drinkRepository.findAll();
        return drinks.stream()
                .map(drink -> {
                    Optional<PriceHistory> latestPrice = priceHistoryRepository.findLatestPriceByDrinkId(drink.getId());
                    return new DrinkWithPrice(
                            drink,
                            latestPrice.map(PriceHistory::getPrice).orElse(null),
                            latestPrice.map(PriceHistory::getTimestamp).orElse(null)
                    );
                })
                .toList();
    }

    public Optional<MonsterDrink> getDrinkById(Long id) {
        return drinkRepository.findById(id);
    }

    public List<PriceHistory> getPriceHistory(Long drinkId) {
        return priceHistoryRepository.findByDrinkIdOrderByTimestampAsc(drinkId);
    }

    @Transactional
    public List<ScraperService.ScrapeResult> syncPrices() {
        return scraperService.scrapeAllPrices();
    }

    public record DrinkWithPrice(
            MonsterDrink drink,
            Double latestPrice,
            java.time.LocalDateTime lastUpdated
    ) {}
}
