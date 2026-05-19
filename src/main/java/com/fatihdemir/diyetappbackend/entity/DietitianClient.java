package com.fatihdemir.diyetappbackend.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(
        name = "dietitian_clients",
        uniqueConstraints = @UniqueConstraint(columnNames = {"dietitian_id", "client_id"})
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DietitianClient extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dietitian_id", nullable = false)
    private DietitianProfile dietitian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientProfile client;

    // Danışanın bu diyetisyenle çalışmaya başladığındaki kilosu
    private Double startWeight;
}
