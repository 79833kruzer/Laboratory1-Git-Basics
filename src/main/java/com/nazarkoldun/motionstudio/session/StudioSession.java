package com.nazarkoldun.motionstudio.session;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** State of one visitor, kept in memory by {@link SessionManager}. */
public class StudioSession {

    private static final int HISTORY_SIZE = 10;

    private final String id;
    private final Instant createdAt = Instant.now();
    private volatile Instant lastSeenAt = createdAt;
    private final AtomicLong requestCount = new AtomicLong();
    private final Set<Long> likedSceneIds = ConcurrentHashMap.newKeySet();
    private final Deque<Long> recentlyPlayed = new ArrayDeque<>();

    StudioSession(String id) {
        this.id = id;
    }

    void touch() {
        lastSeenAt = Instant.now();
        requestCount.incrementAndGet();
    }

    /** @return {@code true} if the scene is liked after the call */
    public boolean toggleLike(Long sceneId) {
        if (likedSceneIds.remove(sceneId)) {
            return false;
        }
        likedSceneIds.add(sceneId);
        return true;
    }

    public boolean likes(Long sceneId) {
        return likedSceneIds.contains(sceneId);
    }

    public synchronized void recordPlay(Long sceneId) {
        recentlyPlayed.remove(sceneId);
        recentlyPlayed.addFirst(sceneId);
        while (recentlyPlayed.size() > HISTORY_SIZE) {
            recentlyPlayed.removeLast();
        }
    }

    public synchronized void forgetScene(Long sceneId) {
        likedSceneIds.remove(sceneId);
        recentlyPlayed.remove(sceneId);
    }

    public synchronized List<Long> getRecentlyPlayed() {
        return List.copyOf(recentlyPlayed);
    }

    public String getId() { return id; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastSeenAt() { return lastSeenAt; }
    public long getRequestCount() { return requestCount.get(); }
    public Set<Long> getLikedSceneIds() { return Set.copyOf(likedSceneIds); }
}
