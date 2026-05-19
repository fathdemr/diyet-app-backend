package com.fatihdemir.diyetappbackend.dto.tags;

import com.fatihdemir.diyetappbackend.entity.Tag;

public record TagResponse(
        Long id,
        String name,
        String type
) {
    public static TagResponse from(Tag t) {
        return new TagResponse(
                t.getId(),
                t.getName(),
                t.getType()
        );
    }
}
