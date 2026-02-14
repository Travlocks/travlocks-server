package org.umc.travlocksserver.domain.traveltheme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.travlocksserver.domain.traveltheme.entity.TravelTheme;

public interface TravelThemeRepository extends JpaRepository<TravelTheme, Long> {}
