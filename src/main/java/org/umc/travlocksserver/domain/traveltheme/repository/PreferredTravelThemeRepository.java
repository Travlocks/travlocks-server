package org.umc.travlocksserver.domain.traveltheme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.travlocksserver.domain.traveltheme.entity.PreferredTravelTheme;

import java.util.List;

public interface PreferredTravelThemeRepository extends JpaRepository<PreferredTravelTheme, Long> {

    @Query("""
        SELECT ptt.travelTheme.id
        FROM PreferredTravelTheme ptt
        WHERE ptt.member.id = :memberId
    """)
    List<Long> findPreferredThemeIdsByMemberId(@Param("memberId") Long memberId);
}