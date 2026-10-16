package com.nazarkoldun.motionstudio.service;

import com.nazarkoldun.motionstudio.api.dto.SceneRequest;
import com.nazarkoldun.motionstudio.model.AnimationPreset;
import com.nazarkoldun.motionstudio.model.Scene;
import com.nazarkoldun.motionstudio.repository.SceneRepository;
import com.nazarkoldun.motionstudio.session.SessionManager;
import com.nazarkoldun.motionstudio.session.StudioSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class SceneService {

    private static final Logger log = LoggerFactory.getLogger(SceneService.class);

    private final SceneRepository scenes;
    private final PresetService presetService;
    private final SessionManager sessionManager;

    public SceneService(SceneRepository scenes, PresetService presetService, SessionManager sessionManager) {
        this.scenes = scenes;
        this.presetService = presetService;
        this.sessionManager = sessionManager;
    }

    public List<Scene> findAll(String query) {
        if (query == null || query.isBlank()) {
            return scenes.findAllByOrderByUpdatedAtDesc();
        }
        return scenes.search(query.trim());
    }

    public Scene get(Long id) {
        return scenes.findById(id).orElseThrow(() -> new NotFoundException("Scene", id));
    }

    @Transactional
    public Scene create(SceneRequest request) {
        AnimationPreset preset = presetService.get(request.presetId());
        Scene scene = scenes.save(new Scene(request.title().trim(), request.text(), request.font(),
                request.fontSizePx(), normalizeColor(request.colorFrom()), normalizeColor(request.colorTo()), preset));
        log.info("Created scene #{} '{}' with preset '{}'", scene.getId(), scene.getTitle(), preset.getName());
        return scene;
    }

    @Transactional
    public Scene update(Long id, SceneRequest request) {
        Scene scene = get(id);
        scene.setTitle(request.title().trim());
        scene.setText(request.text());
        scene.setFont(request.font());
        scene.setFontSizePx(request.fontSizePx());
        scene.setColorFrom(normalizeColor(request.colorFrom()));
        scene.setColorTo(normalizeColor(request.colorTo()));
        scene.setPreset(presetService.get(request.presetId()));
        log.info("Updated scene #{} '{}'", id, scene.getTitle());
        return scenes.saveAndFlush(scene);
    }

    @Transactional
    public void delete(Long id) {
        Scene scene = get(id);
        scenes.delete(scene);
        sessionManager.forgetScene(id);
        log.info("Deleted scene #{} '{}'", id, scene.getTitle());
    }

    /** Registers that the scene was played and returns its fresh state. */
    @Transactional
    public Scene play(Long id, StudioSession session) {
        if (scenes.incrementPlays(id) == 0) {
            throw new NotFoundException("Scene", id);
        }
        session.recordPlay(id);
        return get(id);
    }

    /** Likes or un-likes a scene for the current session (one like per session). */
    @Transactional
    public Scene toggleLike(Long id, StudioSession session) {
        get(id);
        boolean liked = session.toggleLike(id);
        scenes.changeLikes(id, liked ? 1 : -1);
        return get(id);
    }

    private static String normalizeColor(String hex) {
        return hex.toLowerCase(Locale.ROOT);
    }
}
