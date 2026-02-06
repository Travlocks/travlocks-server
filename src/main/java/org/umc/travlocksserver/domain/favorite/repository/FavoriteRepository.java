package org.umc.travlocksserver.domain.favorite.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.travlocksserver.domain.favorite.entity.Favorite;
import org.umc.travlocksserver.domain.template.dto.response.TemplateCardResponseDTO;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

	boolean existsByMemberIdAndTemplateId(Long memberId, Long templateId);

	Optional<Favorite> findByMemberIdAndTemplateId(Long memberId, Long templateId);

	@Query("""
		SELECT new org.umc.travlocksserver.domain.template.dto.response.TemplateCardResponseDTO(
			t.id,
			t.coverImageUrl,
			t.title,
			tt.id,
			tt.content,
			o.id,
			o.nickname,
			t.avgRating,
			t.favoriteCount
		)
		FROM Favorite f
			JOIN f.template t
			JOIN t.owner o
			JOIN t.travelTheme tt
		WHERE f.member.id = :memberId
		ORDER BY f.createdAt DESC, f.id DESC
	""")
	Page<TemplateCardResponseDTO> findMyFavoriteTemplates(@Param("memberId") Long memberId, Pageable pageable);


	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("delete from Favorite f where f.member.id = :memberId")
	void deleteByMemberId(@io.lettuce.core.dynamic.annotation.Param("memberId") Long memberId);
}