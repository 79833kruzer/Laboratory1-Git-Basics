package com.nazarkoldun.motionstudio.api;

import com.nazarkoldun.motionstudio.api.dto.SessionResponse;
import com.nazarkoldun.motionstudio.session.SessionFilter;
import com.nazarkoldun.motionstudio.session.SessionManager;
import com.nazarkoldun.motionstudio.session.StudioSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    private final SessionManager sessionManager;

    public SessionController(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @GetMapping
    public SessionResponse current(@RequestAttribute(SessionFilter.REQUEST_ATTRIBUTE) StudioSession session) {
        return SessionResponse.from(session, sessionManager.activeCount());
    }
}
