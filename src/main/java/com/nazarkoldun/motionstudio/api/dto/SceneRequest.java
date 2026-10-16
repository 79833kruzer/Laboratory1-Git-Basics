package com.nazarkoldun.motionstudio.api.dto;

import com.nazarkoldun.motionstudio.model.StudioFont;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SceneRequest(
        @NotBlank @Size(max = 80) String title,
        @NotBlank @Size(max = 300) String text,
        @NotNull StudioFont font,
        @NotNull @Min(16) @Max(160) Integer fontSizePx,
        @NotNull @Pattern(regexp = HEX, message = "must be a color like #ff00aa") String colorFrom,
        @NotNull @Pattern(regexp = HEX, message = "must be a color like #ff00aa") String colorTo,
        @NotNull Long presetId
) {
    static final String HEX = "^#[0-9a-fA-F]{6}$";
}
