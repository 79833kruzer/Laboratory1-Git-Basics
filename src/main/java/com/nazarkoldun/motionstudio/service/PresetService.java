package com.nazarkoldun.motionstudio.service;

import com.nazarkoldun.motionstudio.api.dto.PresetRequest;
import com.nazarkoldun.motionstudio.model.AnimationPreset;
import com.nazarkoldun.motionstudio.repository.AnimationPresetRepository;
import com.nazarkoldun.motionstudio.repository.SceneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PresetService {

    private static final Logger log = LoggerFactory.getLogger(PresetService.class);

    private final AnimationPresetRepository presets;
    private final SceneRepository scenes;

    public PresetService(AnimationPresetRepository presets, SceneRepository scenes) {
        this.presets = presets;
        this.scenes = scenes;
    }

    public List<AnimationPreset> findAll() {
        return presets.findAllByOrderByNameAsc();
    }

    public AnimationPreset get(Long id) {
        return presets.findById(id).orElseThrow(() -> new NotFoundException("Preset", id));
    }

    public long usageCount(Long id) {
        return scenes.countByPresetId(id);
    }

    @Transactional
    public AnimationPreset create(PresetRequest request) {
        String name = request.name().trim();
        if (presets.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Preset named '" + name + "' already exists");
        }
        AnimationPreset preset = presets.save(new AnimationPreset(name, request.effect(), request.easing(),
                request.durationMs(), request.delayMs(), request.staggerMs(), request.iterations()));
        log.info("Created preset #{} '{}' ({})", preset.getId(), preset.getName(), preset.getEffect());
        return preset;
    }

    @Transactional
    public AnimationPreset update(Long id, PresetRequest request) {
        AnimationPreset preset = get(id);
        String name = request.name().trim();
        if (presets.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Preset named '" + name + "' already exists");
        }
        preset.setName(name);
        preset.setEffect(request.effect());
        preset.setEasing(request.easing());
        preset.setDurationMs(request.durationMs());
        preset.setDelayMs(request.delayMs());
        preset.setStaggerMs(request.staggerMs());
        preset.setIterations(request.iterations());
        log.info("Updated preset #{} '{}'", id, name);
        return presets.saveAndFlush(preset);
    }

    @Transactional
    public void delete(Long id) {
        AnimationPreset preset = get(id);
        long used = scenes.countByPresetId(id);
        if (used > 0) {
            throw new ConflictException("Preset '" + preset.getName() + "' is used by " + used
                    + " scene(s). Change or delete those scenes first.");
        }
        presets.delete(preset);
        log.info("Deleted preset #{} '{}'", id, preset.getName());
    }
}
