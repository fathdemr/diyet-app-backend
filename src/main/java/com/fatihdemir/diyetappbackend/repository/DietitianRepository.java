package com.fatihdemir.diyetappbackend.repository;

import com.fatihdemir.diyetappbackend.entity.Dietitian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DietitianRepository extends JpaRepository<Dietitian, UUID> {
}
