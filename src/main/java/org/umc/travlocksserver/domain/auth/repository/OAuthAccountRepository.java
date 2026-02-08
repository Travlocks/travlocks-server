package org.umc.travlocksserver.domain.auth.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.umc.travlocksserver.domain.auth.entity.OAuthAccount;
import org.umc.travlocksserver.domain.auth.enums.OAuthProvider;

import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {

    Optional<OAuthAccount> findByProviderAndProviderId(OAuthProvider provider, String providerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from OAuthAccount oa where oa.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);

}