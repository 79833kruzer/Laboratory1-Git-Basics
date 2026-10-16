package com.nazarkoldun.motionstudio;

import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:motionstudio-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class MotionStudioApiTest {

    @Autowired
    MockMvc mvc;

    @Test
    void seededDataAndMetaAreAvailable() throws Exception {
        mvc.perform(get("/api/meta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.effects.length()").value(14))
                .andExpect(header().exists("X-Request-Id"));
        mvc.perform(get("/api/presets")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(14));
        mvc.perform(get("/api/scenes")).andExpect(status().isOk()).andExpect(jsonPath("$[0].preset.cssName").exists());
    }

    @Test
    void fullCrudFlowWithSessionLikes() throws Exception {
        MvcResult presetResult = mvc.perform(post("/api/presets").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name":"Test Wave","effect":"WAVE","easing":"EASE","durationMs":1000,
                         "delayMs":0,"staggerMs":50,"iterations":2}"""))
                .andExpect(status().isCreated())
                .andExpect(cookie().exists("MS_SID"))
                .andReturn();
        Cookie sid = presetResult.getResponse().getCookie("MS_SID");
        int presetId = JsonPath.read(presetResult.getResponse().getContentAsString(), "$.id");

        MvcResult sceneResult = mvc.perform(post("/api/scenes").cookie(sid).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Test","text":"Hello tests","font":"MONO","fontSizePx":40,
                                 "colorFrom":"#FF0000","colorTo":"#00ff00","presetId":%d}""".formatted(presetId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.colorFrom").value("#ff0000"))
                .andReturn();
        int sceneId = JsonPath.read(sceneResult.getResponse().getContentAsString(), "$.id");

        mvc.perform(post("/api/scenes/{id}/play", sceneId).cookie(sid))
                .andExpect(status().isOk()).andExpect(jsonPath("$.plays").value(1));
        mvc.perform(post("/api/scenes/{id}/like", sceneId).cookie(sid))
                .andExpect(jsonPath("$.likes").value(1)).andExpect(jsonPath("$.likedByMe").value(true));
        mvc.perform(post("/api/scenes/{id}/like", sceneId).cookie(sid))
                .andExpect(jsonPath("$.likes").value(0)).andExpect(jsonPath("$.likedByMe").value(false));
        mvc.perform(get("/api/session").cookie(sid))
                .andExpect(jsonPath("$.recentlyPlayed[0]").value(sceneId));

        mvc.perform(delete("/api/presets/{id}", presetId)).andExpect(status().isConflict());
        mvc.perform(delete("/api/scenes/{id}", sceneId)).andExpect(status().isNoContent());
        mvc.perform(get("/api/scenes/{id}", sceneId)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/presets/{id}", presetId)).andExpect(status().isNoContent());
    }

    @Test
    void invalidInputIsRejected() throws Exception {
        mvc.perform(post("/api/presets").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name":"","effect":"WAVE","easing":"EASE","durationMs":5,
                         "delayMs":0,"staggerMs":50,"iterations":1}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.durationMs").exists());
        mvc.perform(post("/api/presets").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name":"Soft Fade","effect":"FADE","easing":"EASE","durationMs":500,
                         "delayMs":0,"staggerMs":50,"iterations":1}"""))
                .andExpect(status().isConflict());
    }
}
