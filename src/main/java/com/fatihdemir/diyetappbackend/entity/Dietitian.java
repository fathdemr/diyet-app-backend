package com.fatihdemir.diyetappbackend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
public class Dietitian extends BaseEntity {

    private LocalDate birthDay;
    private String bio;
    private Integer experienceYear;
    private String city;
    private String university; // University modelinden String şekilde id gelecek


    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
