package org.umc.travlocksserver.domain.travelstyle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.travlocksserver.domain.travelstyle.entity.TravelStyle;

public interface TravelStyleRepository extends JpaRepository<TravelStyle, Long> {}
