package com.nazarkoldun.motionstudio.repository;

import com.nazarkoldun.motionstudio.model.AnimationPreset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimationPresetRepository extends JpaRepository<AnimationPreset, Long> {

    List<AnimationPreset> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
