package com.nazarkoldun.motionstudio.api.dto;

import com.nazarkoldun.motionstudio.session.SessionManager;
import com.nazarkoldun.motionstudio.session.StudioSession;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public record SessionResponse(
        String id,
        Instant createdAt,
        Instant lastSeenAt,
        long requestCount,
        Set<Long> likedSceneIds,
        List<Long> recentlyPlayed,
        int activeSessions
) {
    public static SessionResponse from(StudioSession s, int activeSessions) {
        return new SessionResponse(SessionManager.shortId(s.getId()), s.getCreatedAt(), s.getLastSeenAt(),
                s.getRequestCount(), s.getLikedSceneIds(), s.getRecentlyPlayed(), activeSessions);
    }
}
