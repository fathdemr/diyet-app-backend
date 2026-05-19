package com.fatihdemir.diyetappbackend.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "dietitian_profiles")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DietitianProfile extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @EqualsAndHashCode.Include
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
    private Integer experienceYear;
    private String city;
    private String university; // University modelinden String şekilde id gelecek

    @ManyToMany
    @JoinTable(
            name = "dietitian_tags",
            joinColumns = @JoinColumn(name = "dietitian_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
}