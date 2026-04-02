package com.monsterloco.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "monster_drinks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonsterDrink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String flavorName;

    @Column(unique = true, nullable = false)
    private String productCode;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String sourceUrl;

    @Column(nullable = false)
    private boolean isMangoLoco;

    @OneToMany(mappedBy = "drink", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<PriceHistory> priceHistory = new ArrayList<>();
}
