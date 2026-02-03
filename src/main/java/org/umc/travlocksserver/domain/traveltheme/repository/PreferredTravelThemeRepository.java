package org.umc.travlocksserver.domain.traveltheme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.travlocksserver.domain.traveltheme.entity.PreferredTravelTheme;

import java.util.List;

public interface PreferredTravelThemeRepository extends JpaRepository<PreferredTravelTheme, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PreferredTravelTheme ptt where ptt.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);

    @Query("""
        SELECT ptt.travelTheme.id
        FROM PreferredTravelTheme ptt
        WHERE ptt.member.id = :memberId
    """)
    List<Long> findPreferredThemeIdsByMemberId(@Param("memberId") Long memberId);
}