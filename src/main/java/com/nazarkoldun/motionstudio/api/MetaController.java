package com.nazarkoldun.motionstudio.api;

import com.nazarkoldun.motionstudio.api.dto.MetaResponse;
import com.nazarkoldun.motionstudio.model.Easing;
import com.nazarkoldun.motionstudio.model.EffectType;
import com.nazarkoldun.motionstudio.model.StudioFont;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/api/meta")
public class MetaController {

    private static final MetaResponse META = new MetaResponse(
            Arrays.stream(EffectType.values())
                    .map(e -> new MetaResponse.Effect(e.name(), e.getLabel(), e.getCssName(), e.getSplit().name(), e.getDescription()))
                    .toList(),
            Arrays.stream(Easing.values())
                    .map(e -> new MetaResponse.Option(e.name(), e.getLabel(), e.getCss()))
                    .toList(),
            Arrays.stream(StudioFont.values())
                    .map(f -> new MetaResponse.Option(f.name(), f.getLabel(), f.getCss()))
                    .toList());

    @GetMapping
    public MetaResponse meta() {
        return META;
    }
}
