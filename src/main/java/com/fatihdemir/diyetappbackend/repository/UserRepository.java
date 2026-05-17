package com.fatihdemir.diyetappbackend.repository;

import com.fatihdemir.diyetappbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByFireBaseUid(String firebaseUid);
    Optional<User> findByEmail(String email);
}
