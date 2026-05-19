package com.fatihdemir.diyetappbackend.controller;

import com.fatihdemir.diyetappbackend.dto.tags.TagResponse;
import com.fatihdemir.diyetappbackend.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/api/tags")
    public ResponseEntity<List<TagResponse>> getTags() {
        return ResponseEntity.ok(tagService.getTags());
    }
}
