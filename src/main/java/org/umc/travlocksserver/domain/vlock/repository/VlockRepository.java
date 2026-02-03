package org.umc.travlocksserver.domain.vlock.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;

@Repository
public interface VlockRepository extends JpaRepository<Vlock,Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Vlock v set v.owner.id = :deletedMemberId where v.owner.id = :memberId")
    void transferOwner(@Param("memberId") Long memberId,
                       @Param("deletedMemberId") Long deletedMemberId);
}
