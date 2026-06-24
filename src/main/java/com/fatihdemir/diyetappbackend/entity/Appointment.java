package com.fatihdemir.diyetappbackend.entity;

import com.fatihdemir.diyetappbackend.exception.InvalidAppointmentStateException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "appointments")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false, unique = true)
    private AvailableSlot slot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dietitian_id", nullable = false)
    private Dietitian dietitian;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING;

    private String cancelReason;

    public void approve() {
        if (this.status != AppointmentStatus.PENDING) {
            throw new InvalidAppointmentStateException("onaylama", this.status);
        }
        this.status = AppointmentStatus.APPROVED;
    }

    public void reject(String reason) {
        if (this.status == AppointmentStatus.REJECTED) {
            throw new InvalidAppointmentStateException("reddetme", this.status);
        }
        this.status = AppointmentStatus.REJECTED;
        this.cancelReason = reason;
    }

    public void cancel(String reason) {
        if (this.status == AppointmentStatus.REJECTED || this.status == AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentStateException("iptal etme", this.status);
        }
        this.status = AppointmentStatus.CANCELLED;
        this.cancelReason = reason;
    }

}