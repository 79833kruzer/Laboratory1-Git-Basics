package com.nazarkoldun.motionstudio.repository;

import com.nazarkoldun.motionstudio.model.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SceneRepository extends JpaRepository<Scene, Long> {

    List<Scene> findAllByOrderByUpdatedAtDesc();

    @Query("""
            select s from Scene s
            where lower(s.title) like lower(concat('%', :q, '%'))
               or lower(s.text) like lower(concat('%', :q, '%'))
            order by s.updatedAt desc
            """)
    List<Scene> search(@Param("q") String query);

    long countByPresetId(Long presetId);

    @Modifying(clearAutomatically = true)
    @Query("update Scene s set s.plays = s.plays + 1 where s.id = :id")
    int incrementPlays(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("update Scene s set s.likes = s.likes + :delta where s.id = :id")
    int changeLikes(@Param("id") Long id, @Param("delta") long delta);
}
