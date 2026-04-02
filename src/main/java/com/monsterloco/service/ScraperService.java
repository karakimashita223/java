package com.monsterloco.service;

import com.monsterloco.model.MonsterDrink;
import com.monsterloco.model.PriceHistory;
import com.monsterloco.repository.MonsterDrinkRepository;
import com.monsterloco.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScraperService {

    private final MonsterDrinkRepository drinkRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @Transactional
    public List<ScrapeResult> scrapeAllPrices() {
        List<ScrapeResult> results = new ArrayList<>();
        List<MonsterDrink> drinks = drinkRepository.findAll();

        for (MonsterDrink drink : drinks) {
            try {
                ScrapeResult result = scrapePrice(drink);
                results.add(result);
                Thread.sleep(1000); // Be polite to the server
            } catch (Exception e) {
                log.error("Error scraping drink {}: {}", drink.getProductCode(), e.getMessage());
                results.add(new ScrapeResult(drink.getProductCode(), null, null, false, e.getMessage()));
            }
        }

        return results;
    }

    @Transactional
    public ScrapeResult scrapePrice(MonsterDrink drink) {
        try {
            log.info("Scraping price for: {} from {}", drink.getFlavorName(), drink.getSourceUrl());

            Document doc = Jsoup.connect(drink.getSourceUrl())
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .followRedirects(true)
                    .get();

            // Extract title using the specified selector
            Element titleElement = doc.selectFirst("h1[product-testid=\"productTitle\"]");
            String title = titleElement != null ? titleElement.text().trim() : drink.getTitle();

            // Extract price using the specified selector
            Element priceElement = doc.selectFirst("p[data-testid=\"finalPrice\"]");
            if (priceElement == null) {
                log.warn("Price element not found for drink: {}", drink.getProductCode());
                return new ScrapeResult(drink.getProductCode(), null, title, false, "Price element not found");
            }

            Double price = parsePrice(priceElement.text());

            if (price != null && price > 0) {
                // Update drink title if changed
                if (!title.equals(drink.getTitle())) {
                    drink.setTitle(title);
                    drinkRepository.save(drink);
                }

                // Save price history
                PriceHistory priceHistory = PriceHistory.builder()
                        .drink(drink)
                        .price(price)
                        .timestamp(LocalDateTime.now())
                        .build();
                priceHistoryRepository.save(priceHistory);

                log.info("Successfully scraped price: {} UAH for {}", price, drink.getFlavorName());
                return new ScrapeResult(drink.getProductCode(), price, title, true, null);
            } else {
                return new ScrapeResult(drink.getProductCode(), null, title, false, "Could not parse price");
            }

        } catch (IOException e) {
            log.error("IO error while scraping: {}", e.getMessage());
            return new ScrapeResult(drink.getProductCode(), null, null, false, e.getMessage());
        }
    }

    private Double parsePrice(String priceText) {
        if (priceText == null || priceText.isEmpty()) {
            return null;
        }

        try {
            // Remove currency symbols and whitespace, handle Ukrainian format
            String cleaned = priceText
                    .replaceAll("[^\\d.,]", "")
                    .replaceAll(",", ".")
                    .trim();

            if (cleaned.isEmpty()) {
                return null;
            }

            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            log.warn("Could not parse price: {}", priceText);
            return null;
        }
    }

    public record ScrapeResult(
            String productCode,
            Double price,
            String title,
            boolean success,
            String errorMessage
    ) {}
}
