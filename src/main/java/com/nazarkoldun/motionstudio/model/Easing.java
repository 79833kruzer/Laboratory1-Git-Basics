package com.nazarkoldun.motionstudio.model;

public enum Easing {
    LINEAR("Linear", "linear"),
    EASE("Ease", "ease"),
    EASE_IN("Ease In", "ease-in"),
    EASE_OUT("Ease Out", "ease-out"),
    EASE_IN_OUT("Ease In Out", "ease-in-out"),
    SPRING("Spring", "cubic-bezier(0.34, 1.56, 0.64, 1)"),
    EXPO_OUT("Expo Out", "cubic-bezier(0.16, 1, 0.3, 1)"),
    BACK_IN_OUT("Back In Out", "cubic-bezier(0.68, -0.6, 0.32, 1.6)");

    private final String label;
    private final String css;

    Easing(String label, String css) {
        this.label = label;
        this.css = css;
    }

    public String getLabel() { return label; }
    public String getCss() { return css; }
}
