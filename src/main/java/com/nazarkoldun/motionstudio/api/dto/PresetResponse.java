package com.nazarkoldun.motionstudio.api.dto;

import com.nazarkoldun.motionstudio.model.AnimationPreset;

import java.time.Instant;

public record PresetResponse(
        Long id,
        String name,
        String effect,
        String effectLabel,
        String cssName,
        String split,
        String easing,
        String easingCss,
        int durationMs,
        int delayMs,
        int staggerMs,
        int iterations,
        Instant createdAt,
        Instant updatedAt
) {
    public static PresetResponse from(AnimationPreset p) {
        return new PresetResponse(p.getId(), p.getName(),
                p.getEffect().name(), p.getEffect().getLabel(), p.getEffect().getCssName(), p.getEffect().getSplit().name(),
                p.getEasing().name(), p.getEasing().getCss(),
                p.getDurationMs(), p.getDelayMs(), p.getStaggerMs(), p.getIterations(),
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
