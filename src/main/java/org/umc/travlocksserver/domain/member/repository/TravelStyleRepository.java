package org.umc.travlocksserver.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.travlocksserver.domain.member.entity.TravelStyle;

import java.util.List;

public interface TravelStyleRepository extends JpaRepository<TravelStyle, Long> {
    List<TravelStyle> findAllByIdIn(List<Long> ids);
}
