package com.nazarkoldun.motionstudio.api.dto;

import com.nazarkoldun.motionstudio.model.Scene;
import com.nazarkoldun.motionstudio.session.StudioSession;

import java.time.Instant;

public record SceneResponse(
        Long id,
        String title,
        String text,
        String font,
        String fontCss,
        int fontSizePx,
        String colorFrom,
        String colorTo,
        PresetResponse preset,
        long plays,
        long likes,
        boolean likedByMe,
        Instant createdAt,
        Instant updatedAt
) {
    public static SceneResponse from(Scene s, StudioSession session) {
        return new SceneResponse(s.getId(), s.getTitle(), s.getText(),
                s.getFont().name(), s.getFont().getCss(), s.getFontSizePx(),
                s.getColorFrom(), s.getColorTo(),
                PresetResponse.from(s.getPreset()),
                s.getPlays(), s.getLikes(), session.likes(s.getId()),
                s.getCreatedAt(), s.getUpdatedAt());
    }
}
