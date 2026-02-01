package org.umc.travlocksserver.domain.favorite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.travlocksserver.domain.favorite.entity.Favorite;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByMemberIdAndTemplateId(Long memberId, Long templateId);

    Optional<Favorite> findByMemberIdAndTemplateId(Long memberId, Long templateId);
}