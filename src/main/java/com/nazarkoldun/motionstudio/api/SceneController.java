package com.nazarkoldun.motionstudio.api;

import com.nazarkoldun.motionstudio.api.dto.SceneRequest;
import com.nazarkoldun.motionstudio.api.dto.SceneResponse;
import com.nazarkoldun.motionstudio.service.SceneService;
import com.nazarkoldun.motionstudio.session.SessionFilter;
import com.nazarkoldun.motionstudio.session.StudioSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scenes")
public class SceneController {

    private final SceneService sceneService;

    public SceneController(SceneService sceneService) {
        this.sceneService = sceneService;
    }

    @GetMapping
    public List<SceneResponse> list(@RequestParam(required = false) String q,
                                    @RequestAttribute(SessionFilter.REQUEST_ATTRIBUTE) StudioSession session) {
        return sceneService.findAll(q).stream().map(s -> SceneResponse.from(s, session)).toList();
    }

    @GetMapping("/{id}")
    public SceneResponse get(@PathVariable Long id,
                             @RequestAttribute(SessionFilter.REQUEST_ATTRIBUTE) StudioSession session) {
        return SceneResponse.from(sceneService.get(id), session);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SceneResponse create(@Valid @RequestBody SceneRequest request,
                                @RequestAttribute(SessionFilter.REQUEST_ATTRIBUTE) StudioSession session) {
        return SceneResponse.from(sceneService.create(request), session);
    }

    @PutMapping("/{id}")
    public SceneResponse update(@PathVariable Long id, @Valid @RequestBody SceneRequest request,
                                @RequestAttribute(SessionFilter.REQUEST_ATTRIBUTE) StudioSession session) {
        return SceneResponse.from(sceneService.update(id, request), session);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        sceneService.delete(id);
    }

    @PostMapping("/{id}/play")
    public SceneResponse play(@PathVariable Long id,
                              @RequestAttribute(SessionFilter.REQUEST_ATTRIBUTE) StudioSession session) {
        return SceneResponse.from(sceneService.play(id, session), session);
    }

    @PostMapping("/{id}/like")
    public SceneResponse like(@PathVariable Long id,
                              @RequestAttribute(SessionFilter.REQUEST_ATTRIBUTE) StudioSession session) {
        return SceneResponse.from(sceneService.toggleLike(id, session), session);
    }
}
