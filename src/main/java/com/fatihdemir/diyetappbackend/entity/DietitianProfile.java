package com.fatihdemir.diyetappbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "dietitian_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DietitianProfile extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private UUID userId;

    private String firstName;
    private String lastName;

    @Column(nullable = false)
    private String fullName;

    private LocalDate birthDay;
    private String bio;
    private String specialization;
    private Integer experienceYear;
    private String city;
}