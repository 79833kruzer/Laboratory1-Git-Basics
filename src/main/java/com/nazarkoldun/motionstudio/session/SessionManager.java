package com.nazarkoldun.motionstudio.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {

    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);

    private final Map<String, StudioSession> sessions = new ConcurrentHashMap<>();
    private final Duration ttl;

    public SessionManager(@Value("${studio.session.ttl:PT30M}") Duration ttl) {
        this.ttl = ttl;
    }

    public StudioSession resolve(String sessionId) {
        StudioSession session = Optional.ofNullable(sessionId)
                .map(sessions::get)
                .filter(s -> !isExpired(s))
                .orElseGet(this::create);
        session.touch();
        return session;
    }

    public Optional<StudioSession> find(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public void forgetScene(Long sceneId) {
        sessions.values().forEach(s -> s.forgetScene(sceneId));
    }

    public int activeCount() {
        return sessions.size();
    }

    @Scheduled(fixedDelayString = "${studio.session.cleanup-interval:PT1M}")
    public void evictExpired() {
        int before = sessions.size();
        sessions.values().removeIf(this::isExpired);
        int removed = before - sessions.size();
        if (removed > 0) {
            log.info("Evicted {} expired session(s), {} active", removed, sessions.size());
        }
    }

    private StudioSession create() {
        StudioSession session = new StudioSession(UUID.randomUUID().toString());
        sessions.put(session.getId(), session);
        log.info("New session {} ({} active)", shortId(session.getId()), sessions.size());
        return session;
    }

    private boolean isExpired(StudioSession session) {
        return session.getLastSeenAt().plus(ttl).isBefore(Instant.now());
    }

    public static String shortId(String sessionId) {
        return sessionId == null ? "-" : sessionId.substring(0, Math.min(8, sessionId.length()));
    }
}
