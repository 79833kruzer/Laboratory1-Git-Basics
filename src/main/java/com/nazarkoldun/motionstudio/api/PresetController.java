package com.nazarkoldun.motionstudio.api;

import com.nazarkoldun.motionstudio.api.dto.PresetRequest;
import com.nazarkoldun.motionstudio.api.dto.PresetResponse;
import com.nazarkoldun.motionstudio.service.PresetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/presets")
public class PresetController {

    private final PresetService presetService;

    public PresetController(PresetService presetService) {
        this.presetService = presetService;
    }

    @GetMapping
    public List<PresetResponse> list() {
        return presetService.findAll().stream().map(PresetResponse::from).toList();
    }

    @GetMapping("/{id}")
    public PresetResponse get(@PathVariable Long id) {
        return PresetResponse.from(presetService.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PresetResponse create(@Valid @RequestBody PresetRequest request) {
        return PresetResponse.from(presetService.create(request));
    }

    @PutMapping("/{id}")
    public PresetResponse update(@PathVariable Long id, @Valid @RequestBody PresetRequest request) {
        return PresetResponse.from(presetService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        presetService.delete(id);
    }
}
