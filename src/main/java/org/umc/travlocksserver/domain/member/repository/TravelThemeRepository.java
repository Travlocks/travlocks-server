package org.umc.travlocksserver.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.travlocksserver.domain.member.entity.TravelTheme;

import java.util.List;

public interface TravelThemeRepository extends JpaRepository<TravelTheme, Long> {
    List<TravelTheme> findAllByIdIn(List<Long> ids);
}