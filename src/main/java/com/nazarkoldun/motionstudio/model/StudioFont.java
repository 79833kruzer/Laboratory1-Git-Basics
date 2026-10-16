package com.nazarkoldun.motionstudio.model;

public enum StudioFont {
    PLAYFAIR("Playfair Display", "'Playfair Display', Georgia, serif"),
    SPACE_GROTESK("Space Grotesk", "'Space Grotesk', system-ui, sans-serif"),
    BEBAS("Bebas Neue", "'Bebas Neue', Impact, sans-serif"),
    PACIFICO("Pacifico", "'Pacifico', cursive"),
    MONO("JetBrains Mono", "'JetBrains Mono', Consolas, monospace"),
    SYSTEM("System UI", "system-ui, 'Segoe UI', sans-serif");

    private final String label;
    private final String css;

    StudioFont(String label, String css) {
        this.label = label;
        this.css = css;
    }

    public String getLabel() { return label; }
    public String getCss() { return css; }
}
