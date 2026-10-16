package com.nazarkoldun.motionstudio.config;

import com.nazarkoldun.motionstudio.model.AnimationPreset;
import com.nazarkoldun.motionstudio.model.Easing;
import com.nazarkoldun.motionstudio.model.EffectType;
import com.nazarkoldun.motionstudio.model.Scene;
import com.nazarkoldun.motionstudio.model.StudioFont;
import com.nazarkoldun.motionstudio.repository.AnimationPresetRepository;
import com.nazarkoldun.motionstudio.repository.SceneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Fills an empty database with a starter set of presets and scenes. */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AnimationPresetRepository presets;
    private final SceneRepository scenes;

    public DataSeeder(AnimationPresetRepository presets, SceneRepository scenes) {
        this.presets = presets;
        this.scenes = scenes;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (presets.count() > 0) {
            return;
        }
        AnimationPreset springRise = preset("Spring Rise", EffectType.RISE, Easing.SPRING, 800, 0, 40, 1);
        AnimationPreset rain = preset("Letter Rain", EffectType.DROP, Easing.EXPO_OUT, 900, 0, 60, 1);
        AnimationPreset ocean = preset("Ocean Wave", EffectType.WAVE, Easing.EASE_IN_OUT, 1400, 0, 70, 0);
        AnimationPreset glitch = preset("Cyber Glitch", EffectType.GLITCH, Easing.LINEAR, 700, 0, 25, 1);
        AnimationPreset neon = preset("Night Neon", EffectType.NEON, Easing.LINEAR, 1600, 0, 80, 1);
        AnimationPreset typing = preset("Old Typewriter", EffectType.TYPEWRITER, Easing.LINEAR, 100, 0, 85, 1);
        preset("Soft Fade", EffectType.FADE, Easing.EASE_OUT, 900, 0, 45, 1);
        preset("Cinema Focus", EffectType.BLUR_IN, Easing.EASE_OUT, 1200, 200, 35, 1);
        preset("Pop Zoom", EffectType.ZOOM, Easing.SPRING, 600, 0, 30, 1);
        preset("Card Flip", EffectType.FLIP, Easing.EXPO_OUT, 900, 0, 50, 1);
        preset("Happy Bounce", EffectType.BOUNCE, Easing.EASE_OUT, 1000, 0, 55, 1);
        preset("Rubber Band", EffectType.ELASTIC, Easing.EASE_OUT, 1100, 0, 40, 1);
        preset("Hanging Signs", EffectType.SWING, Easing.EASE_OUT, 1000, 0, 140, 1);
        preset("Word Slide", EffectType.SLIDE_WORDS, Easing.EXPO_OUT, 800, 0, 120, 1);

        scenes.save(new Scene("Welcome", "Welcome to Motion Studio", StudioFont.PLAYFAIR, 72, "#f9a8d4", "#a78bfa", springRise));
        scenes.save(new Scene("Hello World", "Hello, World!", StudioFont.MONO, 64, "#4ade80", "#22d3ee", typing));
        scenes.save(new Scene("Open Late", "OPEN ALL NIGHT", StudioFont.BEBAS, 96, "#f472b6", "#38bdf8", neon));
        scenes.save(new Scene("System Error", "SYSTEM FAILURE", StudioFont.SPACE_GROTESK, 80, "#f87171", "#fbbf24", glitch));
        scenes.save(new Scene("Summer", "Endless summer", StudioFont.PACIFICO, 76, "#fde047", "#fb923c", ocean));
        scenes.save(new Scene("Git Commit", "git commit -m \"magic\"", StudioFont.MONO, 52, "#93c5fd", "#c4b5fd", rain));
        log.info("Seeded {} presets and {} scenes", presets.count(), scenes.count());
    }

    private AnimationPreset preset(String name, EffectType effect, Easing easing,
                                   int duration, int delay, int stagger, int iterations) {
        return presets.save(new AnimationPreset(name, effect, easing, duration, delay, stagger, iterations));
    }
}
