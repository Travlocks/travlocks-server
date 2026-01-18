package org.umc.travlocksserver.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.travlocksserver.domain.member.entity.mapping.PreferredTravelTheme;

public interface PreferredTravelThemeRepository extends JpaRepository<PreferredTravelTheme, Long> {}