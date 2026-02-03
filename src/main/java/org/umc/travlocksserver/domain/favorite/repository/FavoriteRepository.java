package org.umc.travlocksserver.domain.favorite.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.umc.travlocksserver.domain.favorite.entity.Favorite;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByMemberIdAndTemplateId(Long memberId, Long templateId);

    Optional<Favorite> findByMemberIdAndTemplateId(Long memberId, Long templateId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Favorite f where f.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);
}