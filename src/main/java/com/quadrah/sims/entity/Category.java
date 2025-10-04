package com.quadrah.sims.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
@Table(name = "categories")
public class Category extends BaseEntity{

    @Column(nullable = false, unique = true)
    private String name;
}
