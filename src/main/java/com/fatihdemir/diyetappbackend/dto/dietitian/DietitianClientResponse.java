package com.fatihdemir.diyetappbackend.dto.dietitian;

import com.fatihdemir.diyetappbackend.entity.ClientProgressStatus;
import com.fatihdemir.diyetappbackend.entity.DietitianClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record DietitianClientResponse(
        UUID clientId,
        String fullName,
        String goal,
        LocalDate birthDay,
        Double startWeight,
        Double currentWeight,
        Double weightChange,
        ClientProgressStatus status,
        LocalDateTime enrolledAt
) {
    public static DietitianClientResponse from(DietitianClient dc) {
        var client = dc.getClient();
        Double start = dc.getStartWeight();
        Double current = client.getWeight();
        Double change = (start != null && current != null) ? current - start : null;

        ClientProgressStatus status;
        if (change == null) {
            status = ClientProgressStatus.STABLE;
        } else if (change < -0.1) {
            status = ClientProgressStatus.ON_TRACK;
        } else if (change > 0.1) {
            status = ClientProgressStatus.NEEDS_ATTENTION;
        } else {
            status = ClientProgressStatus.STABLE;
        }

        return new DietitianClientResponse(
                client.getUserId(),
                client.getFullName(),
                client.getGoal(),
                client.getBirthDay(),
                start,
                current,
                change,
                status,
                dc.getCreatedAt()
        );
    }
}
