package org.umc.travlocksserver.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.travlocksserver.domain.member.entity.mapping.PreferredTravelStyle;

public interface PreferredTravelStyleRepository extends JpaRepository<PreferredTravelStyle, Long> {}

