package com.nazarkoldun.motionstudio.model;

/**
 * Text effects supported by the studio. Every constant maps to a CSS
 * {@code @keyframes} rule in the frontend (see {@code styles.css}).
 */
public enum EffectType {
    FADE("Fade In", "ms-fade", Split.CHAR, "Letters softly appear one after another"),
    RISE("Rise", "ms-rise", Split.CHAR, "Letters float up from below"),
    DROP("Drop", "ms-drop", Split.CHAR, "Letters fall down from above"),
    BLUR_IN("Blur Focus", "ms-blur", Split.CHAR, "Out-of-focus letters snap into focus"),
    ZOOM("Zoom", "ms-zoom", Split.CHAR, "Letters grow from nothing"),
    FLIP("Flip", "ms-flip", Split.CHAR, "Letters flip around the X axis"),
    BOUNCE("Bounce", "ms-bounce", Split.CHAR, "Letters bounce into place"),
    ELASTIC("Elastic", "ms-elastic", Split.CHAR, "Letters stretch like rubber"),
    WAVE("Wave", "ms-wave", Split.CHAR, "A wave running through the text (best on loop)"),
    GLITCH("Glitch", "ms-glitch", Split.CHAR, "Digital glitch with jitter and color split"),
    NEON("Neon", "ms-neon", Split.CHAR, "Flickering neon sign"),
    TYPEWRITER("Typewriter", "ms-type", Split.CHAR, "Characters are typed one by one"),
    SWING("Swing Words", "ms-swing", Split.WORD, "Words swing in like hanging signs"),
    SLIDE_WORDS("Slide Words", "ms-slide", Split.WORD, "Words slide in from the left");

    public enum Split { CHAR, WORD }

    private final String label;
    private final String cssName;
    private final Split split;
    private final String description;

    EffectType(String label, String cssName, Split split, String description) {
        this.label = label;
        this.cssName = cssName;
        this.split = split;
        this.description = description;
    }

    public String getLabel() { return label; }
    public String getCssName() { return cssName; }
    public Split getSplit() { return split; }
    public String getDescription() { return description; }
}
