package com.monsterloco.controller;

import com.monsterloco.model.PriceHistory;
import com.monsterloco.service.DrinkService;
import com.monsterloco.service.ScraperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DrinkController {

    private final DrinkService drinkService;

    @GetMapping("/drinks")
    public ResponseEntity<List<DrinkService.DrinkWithPrice>> getAllDrinks() {
        return ResponseEntity.ok(drinkService.getAllDrinksWithLatestPrice());
    }

    @GetMapping("/drinks/{id}")
    public ResponseEntity<?> getDrinkById(@PathVariable Long id) {
        return drinkService.getDrinkById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/drinks/{id}/history")
    public ResponseEntity<List<PriceHistory>> getPriceHistory(@PathVariable Long id) {
        List<PriceHistory> history = drinkService.getPriceHistory(id);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/sync")
    public ResponseEntity<SyncResponse> syncPrices() {
        List<ScraperService.ScrapeResult> results = drinkService.syncPrices();

        int successCount = (int) results.stream().filter(ScraperService.ScrapeResult::success).count();
        int failCount = results.size() - successCount;

        return ResponseEntity.ok(new SyncResponse(
                "Price sync completed",
                successCount,
                failCount,
                results
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Monster Loco Tracker is running!");
    }

    public record SyncResponse(
            String message,
            int successCount,
            int failCount,
            List<ScraperService.ScrapeResult> results
    ) {}
}
