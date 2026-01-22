package org.umc.travlocksserver.domain.traveltheme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.travlocksserver.domain.traveltheme.entity.PreferredTravelTheme;

public interface PreferredTravelThemeRepository extends JpaRepository<PreferredTravelTheme, Long> {}