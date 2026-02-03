package org.umc.travlocksserver.domain.member.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.umc.travlocksserver.domain.member.entity.MemberConsent;

public interface MemberConsentRepository extends JpaRepository<MemberConsent, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from MemberConsent mc where mc.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);

}
