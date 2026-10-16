package com.nazarkoldun.motionstudio.api.dto;

import java.util.List;

/** Dictionaries the frontend needs to build its forms. */
public record MetaResponse(List<Effect> effects, List<Option> easings, List<Option> fonts) {

    public record Effect(String code, String label, String cssName, String split, String description) {
    }

    public record Option(String code, String label, String css) {
    }
}
