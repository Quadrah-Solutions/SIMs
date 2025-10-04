package com.quadrah.sims.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "inventory",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"product_id", "batch_number"}))
public class Inventory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "batch_number", nullable = false)
    private String batchNumber;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "cost_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "barcode_data")
    private String barcodeData;

    @Column(name = "barcode_image")
    @JdbcTypeCode(SqlTypes.BIGINT)
    private Long barcodeImageOid;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL)
    private Set<TransactionItem> transactionItems = new HashSet<>();

}