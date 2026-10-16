package com.nazarkoldun.motionstudio.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "animation_presets")
public class AnimationPreset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EffectType effect;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Easing easing;

    @Column(nullable = false)
    private int durationMs;

    @Column(nullable = false)
    private int delayMs;

    @Column(nullable = false)
    private int staggerMs;

    @Column(nullable = false)
    private int iterations;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected AnimationPreset() {
    }

    public AnimationPreset(String name, EffectType effect, Easing easing,
                           int durationMs, int delayMs, int staggerMs, int iterations) {
        this.name = name;
        this.effect = effect;
        this.easing = easing;
        this.durationMs = durationMs;
        this.delayMs = delayMs;
        this.staggerMs = staggerMs;
        this.iterations = iterations;
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
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public EffectType getEffect() { return effect; }
    public void setEffect(EffectType effect) { this.effect = effect; }
    public Easing getEasing() { return easing; }
    public void setEasing(Easing easing) { this.easing = easing; }
    public int getDurationMs() { return durationMs; }
    public void setDurationMs(int durationMs) { this.durationMs = durationMs; }
    public int getDelayMs() { return delayMs; }
    public void setDelayMs(int delayMs) { this.delayMs = delayMs; }
    public int getStaggerMs() { return staggerMs; }
    public void setStaggerMs(int staggerMs) { this.staggerMs = staggerMs; }
    public int getIterations() { return iterations; }
    public void setIterations(int iterations) { this.iterations = iterations; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
