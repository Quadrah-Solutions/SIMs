package com.quadrah.sims.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Patient extends BaseEntity {
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String gender;
    private String address;
    private String phoneNumber;
    private String email;

//    @ManyToOne
//    @JoinColumn(name = "insurance_id")
//    private Insurance insurance;

//    @OneToOne
//    @JoinColumn(name = "user_id")
//    private User user;
}