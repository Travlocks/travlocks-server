package org.umc.travlocksserver.domain.favorite.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.travlocksserver.domain.favorite.entity.Favorite;
import org.umc.travlocksserver.domain.template.entity.Template;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

	boolean existsByMemberIdAndTemplateId(Long memberId, Long templateId);

	Optional<Favorite> findByMemberIdAndTemplateId(Long memberId, Long templateId);

	@Query("""
		    select f.template
		    from Favorite f
		    where f.member.id = :memberId
		    order by f.template.id desc
		""")
	List<Template> findFavoriteTemplatesFirst(@Param("memberId") Long memberId, Pageable pageable);

	@Query("""
		    select f.template
		    from Favorite f
		    where f.member.id = :memberId
		      and f.template.id < :cursor
		    order by f.template.id desc
		""")
	List<Template> findFavoriteTemplatesAfterCursor(@Param("memberId") Long memberId, @Param("cursor") Long cursor, Pageable pageable);

	default List<Template> findFavoriteTemplatesFirst(Long memberId, int limit) {
		return findFavoriteTemplatesFirst(memberId, PageRequest.of(0, limit));
	}

	default List<Template> findFavoriteTemplatesAfterCursor(Long memberId, Long cursor, int limit) {
		return findFavoriteTemplatesAfterCursor(memberId, cursor, PageRequest.of(0, limit));
	}
}