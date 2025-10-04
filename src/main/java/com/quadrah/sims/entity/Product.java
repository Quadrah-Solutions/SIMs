package com.quadrah.sims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "products")
public class Product extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    private String category;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "requires_prescription", nullable = false)
    private boolean requiresPrescription = false;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private Set<Inventory> inventoryItems = new HashSet<>();

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold = 10;

    // Constructors, getters, setters
}
