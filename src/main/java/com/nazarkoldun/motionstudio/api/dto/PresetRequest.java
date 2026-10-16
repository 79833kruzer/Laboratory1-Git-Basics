package com.nazarkoldun.motionstudio.api.dto;

import com.nazarkoldun.motionstudio.model.Easing;
import com.nazarkoldun.motionstudio.model.EffectType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PresetRequest(
        @NotBlank @Size(max = 60) String name,
        @NotNull EffectType effect,
        @NotNull Easing easing,
        @NotNull @Min(100) @Max(10_000) Integer durationMs,
        @NotNull @Min(0) @Max(5_000) Integer delayMs,
        @NotNull @Min(0) @Max(1_000) Integer staggerMs,
        @NotNull @Min(0) @Max(20) Integer iterations
) {
}
