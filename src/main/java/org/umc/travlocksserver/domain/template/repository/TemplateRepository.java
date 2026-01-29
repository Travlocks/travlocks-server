package org.umc.travlocksserver.domain.template.repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.travlocksserver.domain.template.entity.Template;

public interface TemplateRepository extends JpaRepository<Template, Long> {

	@Query("""
		    SELECT t
		    FROM Template t
		    JOIN FETCH t.travelTheme
		    JOIN FETCH t.owner
		    WHERE t.isPublic = true
		      AND t.deletedAt IS NULL
		    ORDER BY
		        t.remixCount DESC,
		        t.favoriteCount DESC,
		        t.avgRating DESC
		""")
	List<Template> findPopularTemplates(Pageable pageable);

	@Query("""
		    select t from Template t
		    where t.owner.id = :memberId
		      and t.isPublic = true
		    order by t.id desc
		""")
	List<Template> findPublicTemplatesFirst(@Param("memberId") Long memberId, Pageable pageable);

	@Query("""
		    select t from Template t
		    where t.owner.id = :memberId
		      and t.isPublic = true
		      and t.id < :cursor
		    order by t.id desc
		""")
	List<Template> findPublicTemplatesAfterCursor(@Param("memberId") Long memberId, @Param("cursor") Long cursor,
		Pageable pageable);

	default List<Template> findPublicTemplatesFirst(Long memberId, int limit) {
		return findPublicTemplatesFirst(memberId, PageRequest.of(0, limit));
	}

	default List<Template> findPublicTemplatesAfterCursor(Long memberId, Long cursor, int limit) {
		return findPublicTemplatesAfterCursor(memberId, cursor, PageRequest.of(0, limit));
	}
}