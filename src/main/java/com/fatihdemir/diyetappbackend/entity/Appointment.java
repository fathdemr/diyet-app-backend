package com.fatihdemir.diyetappbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
public class Appointment extends BaseEntity {

    @Column(nullable = false, length = 100)
    private LocalDate appointmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    private String cancelReason;

    public void approve() {
        if (this.status != AppointmentStatus.PENDING) {
            throw new IllegalStateException("Cannot confirm order in status: " + this.status);
        }
        this.status = AppointmentStatus.APPROVED;
    }

    public void reject() {
        if (this.status == AppointmentStatus.REJECTED) {
            throw new IllegalStateException("Cannot reject order in status: " + this.status);
        }
        this.status = AppointmentStatus.REJECTED;
    }
}
