package com.fatihdemir.diyetappbackend.service;

import com.fatihdemir.diyetappbackend.dto.tags.TagResponse;
import com.fatihdemir.diyetappbackend.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<TagResponse> getTags() {
        return tagRepository.findAll()
                .stream()
                .map(TagResponse::from)
                .toList();
    }
}
