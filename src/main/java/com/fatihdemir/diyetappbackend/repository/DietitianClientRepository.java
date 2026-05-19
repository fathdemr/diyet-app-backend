package com.fatihdemir.diyetappbackend.repository;

import com.fatihdemir.diyetappbackend.entity.DietitianClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DietitianClientRepository extends JpaRepository<DietitianClient, UUID> {

    @Query(
            value = "SELECT dc FROM DietitianClient dc JOIN FETCH dc.client WHERE dc.dietitian.id = :dietitianId",
            countQuery = "SELECT COUNT(dc) FROM DietitianClient dc WHERE dc.dietitian.id = :dietitianId"
    )
    Page<DietitianClient> findByDietitianIdWithClient(@Param("dietitianId") UUID dietitianId, Pageable pageable);

    boolean existsByDietitian_IdAndClient_Id(UUID dietitianId, UUID clientId);

    Optional<DietitianClient> findByDietitian_IdAndClient_Id(UUID dietitianId, UUID clientId);
}
