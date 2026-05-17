package com.fatihdemir.diyetappbackend.service;

import com.fatihdemir.diyetappbackend.dto.PageResponse;
import com.fatihdemir.diyetappbackend.dto.appointment.AppointmentRequest;
import com.fatihdemir.diyetappbackend.dto.appointment.AppointmentResponse;
import com.fatihdemir.diyetappbackend.entity.Appointment;
import com.fatihdemir.diyetappbackend.entity.AppointmentStatus;
import com.fatihdemir.diyetappbackend.exception.AppointmentException;
import com.fatihdemir.diyetappbackend.repository.AppointmentRepository;
import com.fatihdemir.diyetappbackend.repository.ClientProfileRepository;
import com.fatihdemir.diyetappbackend.repository.DietitianAvailabilityRepository;
import com.fatihdemir.diyetappbackend.repository.DietitianProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final List<AppointmentStatus> ACTIVE_STATUSES =
            List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);

    private final AppointmentRepository appointmentRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final DietitianProfileRepository dietitianProfileRepository;
    private final DietitianAvailabilityRepository availabilityRepository;

    @Transactional
    public AppointmentResponse createAppointment(String principalUserId, AppointmentRequest request) {
        var client = clientProfileRepository.findByUserId(UUID.fromString(principalUserId))
                .orElseThrow(() -> new AppointmentException("Danışan profili bulunamadı", HttpStatus.NOT_FOUND));

        // PESSIMISTIC_WRITE: aynı diyetisyenin aynı slotuna eş zamanlı iki istek gelirse
        // ikinci transaction kilidi bekler, birincisi commit edince çakışmayı görür
        var dietitian = dietitianProfileRepository.findByIdWithLock(request.dietitianId())
                .orElseThrow(() -> new AppointmentException("Diyetisyen bulunamadı", HttpStatus.NOT_FOUND));

        if (!request.startDateTime().isBefore(request.endDateTime())) {
            throw new AppointmentException("Bitiş zamanı başlangıç zamanından sonra olmalıdır", HttpStatus.BAD_REQUEST);
        }

        if (request.startDateTime().isBefore(LocalDateTime.now())) {
            throw new AppointmentException("Geçmiş bir tarihe randevu oluşturulamaz", HttpStatus.BAD_REQUEST);
        }

        // Diyetisyen bu saati müsait olarak işaretlemiş mi?
        if (availabilityRepository.countContaining(
                dietitian.getId(), request.startDateTime(), request.endDateTime()) == 0) {
            throw new AppointmentException("Diyetisyen bu saat aralığında müsait değil", HttpStatus.CONFLICT);
        }

        // Çakışan aktif randevu var mı?
        long conflicts = appointmentRepository.countConflicts(
                dietitian.getId(), request.startDateTime(), request.endDateTime(), ACTIVE_STATUSES);

        if (conflicts > 0) {
            throw new AppointmentException("Bu saat aralığı zaten dolu", HttpStatus.CONFLICT);
        }

        var appointment = new Appointment();
        appointment.setClient(client);
        appointment.setDietitian(dietitian);
        appointment.setStartDateTime(request.startDateTime());
        appointment.setEndDateTime(request.endDateTime());
        appointment.setNotes(request.notes());
        appointment.setStatus(AppointmentStatus.PENDING);

        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse confirmAppointment(String principalUserId, UUID appointmentId) {
        var dietitian = dietitianProfileRepository.findByUserId(UUID.fromString(principalUserId))
                .orElseThrow(() -> new AppointmentException("Diyetisyen profili bulunamadı", HttpStatus.NOT_FOUND));

        var appointment = appointmentRepository.findByIdAndDietitianId(appointmentId, dietitian.getId())
                .orElseThrow(() -> new AppointmentException("Randevu bulunamadı", HttpStatus.NOT_FOUND));

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new AppointmentException("Sadece bekleyen randevular onaylanabilir", HttpStatus.BAD_REQUEST);
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse cancelAppointment(String principalUserId, UUID appointmentId, String reason) {
        var userId = UUID.fromString(principalUserId);

        var appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentException("Randevu bulunamadı", HttpStatus.NOT_FOUND));

        boolean isClient = appointment.getClient().getUserId().equals(userId);
        boolean isDietitian = appointment.getDietitian().getUserId().equals(userId);

        if (!isClient && !isDietitian) {
            throw new AppointmentException("Bu randevuyu iptal etme yetkiniz yok", HttpStatus.FORBIDDEN);
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED
                || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new AppointmentException("Bu randevu artık iptal edilemez", HttpStatus.BAD_REQUEST);
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelReason(reason);
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse completeAppointment(String principalUserId, UUID appointmentId) {
        var dietitian = dietitianProfileRepository.findByUserId(UUID.fromString(principalUserId))
                .orElseThrow(() -> new AppointmentException("Diyetisyen profili bulunamadı", HttpStatus.NOT_FOUND));

        var appointment = appointmentRepository.findByIdAndDietitianId(appointmentId, dietitian.getId())
                .orElseThrow(() -> new AppointmentException("Randevu bulunamadı", HttpStatus.NOT_FOUND));

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new AppointmentException("Sadece onaylı randevular tamamlanabilir", HttpStatus.BAD_REQUEST);
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> getMyAppointmentsAsClient(String principalUserId, Pageable pageable) {
        var client = clientProfileRepository.findByUserId(UUID.fromString(principalUserId))
                .orElseThrow(() -> new AppointmentException("Danışan profili bulunamadı", HttpStatus.NOT_FOUND));

        return PageResponse.from(
                appointmentRepository.findByClientIdWithDetails(client.getId(), pageable)
                        .map(AppointmentResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> getMyAppointmentsAsDietitian(String principalUserId, Pageable pageable) {
        var dietitian = dietitianProfileRepository.findByUserId(UUID.fromString(principalUserId))
                .orElseThrow(() -> new AppointmentException("Diyetisyen profili bulunamadı", HttpStatus.NOT_FOUND));

        return PageResponse.from(
                appointmentRepository.findByDietitianIdWithDetails(dietitian.getId(), pageable)
                        .map(AppointmentResponse::from)
        );
    }
}