package org.umc.travlocksserver.domain.travelstyle.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.umc.travlocksserver.domain.travelstyle.entity.PreferredTravelStyle;

import java.util.List;

public interface PreferredTravelStyleRepository extends JpaRepository<PreferredTravelStyle, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PreferredTravelStyle pts where pts.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);


    @Query("""
        SELECT pts.travelStyle.id
        FROM PreferredTravelStyle pts
        WHERE pts.member.id = :memberId
    """)
    List<Long> findPreferredStyleIdsByMemberId(@Param("memberId") Long memberId);
}

