package com.fatihdemir.diyetappbackend.repository;

import com.fatihdemir.diyetappbackend.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
