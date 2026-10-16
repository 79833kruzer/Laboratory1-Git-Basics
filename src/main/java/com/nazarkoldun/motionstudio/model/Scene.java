package com.nazarkoldun.motionstudio.model;

import jakarta.persistence.*;

import java.time.Instant;

/** A piece of styled text together with the animation used to reveal it. */
@Entity
@Table(name = "scenes")
public class Scene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false, length = 300)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StudioFont font;

    @Column(nullable = false)
    private int fontSizePx;

    @Column(nullable = false, length = 7)
    private String colorFrom;

    @Column(nullable = false, length = 7)
    private String colorTo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "preset_id", nullable = false)
    private AnimationPreset preset;

    @Column(nullable = false)
    private long plays;

    @Column(nullable = false)
    private long likes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Scene() {
    }

    public Scene(String title, String text, StudioFont font, int fontSizePx,
                 String colorFrom, String colorTo, AnimationPreset preset) {
        this.title = title;
        this.text = text;
        this.font = font;
        this.fontSizePx = fontSizePx;
        this.colorFrom = colorFrom;
        this.colorTo = colorTo;
        this.preset = preset;
    }

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public StudioFont getFont() { return font; }
    public void setFont(StudioFont font) { this.font = font; }
    public int getFontSizePx() { return fontSizePx; }
    public void setFontSizePx(int fontSizePx) { this.fontSizePx = fontSizePx; }
    public String getColorFrom() { return colorFrom; }
    public void setColorFrom(String colorFrom) { this.colorFrom = colorFrom; }
    public String getColorTo() { return colorTo; }
    public void setColorTo(String colorTo) { this.colorTo = colorTo; }
    public AnimationPreset getPreset() { return preset; }
    public void setPreset(AnimationPreset preset) { this.preset = preset; }
    public long getPlays() { return plays; }
    public long getLikes() { return likes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
