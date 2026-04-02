package com.monsterloco.config;

import com.monsterloco.model.MonsterDrink;
import com.monsterloco.repository.MonsterDrinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MonsterDrinkRepository drinkRepository;

    @Override
    public void run(String... args) {
        if (drinkRepository.count() == 0) {
            log.info("Initializing Monster drinks data...");

            List<MonsterDrink> drinks = List.of(
                    MonsterDrink.builder()
                            .title("Monster Energy Mango Loco")
                            .flavorName("Mango Loco")
                            .productCode("MD_763227")
                            .description("Tropical mango flavor with a smooth, refreshing taste. The legendary Mango Loco!")
                            .sourceUrl("https://maudau.com.ua/product/napii-enerhetychnyi-monster-mangoloco-b-a-haz-z-b")
                            .isMangoLoco(true)
                            .build(),
                    MonsterDrink.builder()
                            .title("Monster Energy Juiced Monarch")
                            .flavorName("Juiced Monarch")
                            .productCode("MD_MONARCH")
                            .description("Peachy orange juice blend with a bold energy kick")
                            .sourceUrl("https://maudau.com.ua/product/enerhetychnyi-napii-monster-energy-juiced-monarch-05-l-917553")
                            .isMangoLoco(false)
                            .build(),
                    MonsterDrink.builder()
                            .title("Monster Energy Original")
                            .flavorName("Original")
                            .productCode("MD_ORIGINAL")
                            .description("The classic Monster Energy that started it all")
                            .sourceUrl("https://maudau.com.ua/product/napii-enerhetychnyi-monster-energy-bezalkoholnyi-05-l-895483")
                            .isMangoLoco(false)
                            .build(),
                    MonsterDrink.builder()
                            .title("Monster Energy Ultra White")
                            .flavorName("Ultra White")
                            .productCode("MD_ULTRA_WHITE")
                            .description("Zero sugar, full flavor. Refreshing citrus taste without the calories")
                            .sourceUrl("https://maudau.com.ua/product/napii-enerhetychnyi-monster-energy-ultra-b-a-z-b")
                            .isMangoLoco(false)
                            .build()
            );

            drinkRepository.saveAll(drinks);
            log.info("Initialized {} Monster drinks", drinks.size());
        } else {
            log.info("Database already contains {} drinks", drinkRepository.count());
        }
    }
}
