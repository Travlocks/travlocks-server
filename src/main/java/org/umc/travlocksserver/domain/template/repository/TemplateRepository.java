package org.umc.travlocksserver.domain.template.repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.template.entity.Template;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long>, TemplateRepositoryCustom {

	@Query("""
		    SELECT DISTINCT t.parentTemplate.id
		    FROM Template t
		    WHERE t.owner.id = :memberId
		        AND t.parentTemplate IS NOT NULL
		""")
	List<Long> findRemixedTemplateIdsByMemberId(@Param("memberId") Long memberId);

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
			select t 
			from Template t
			where t.owner.id = :memberId
			    and t.isPublic = true
			order by t.id desc
		""")
	List<Template> findPublicTemplatesFirst(@Param("memberId") Long memberId, Pageable pageable);

	@Query("""
		    SELECT t.travelTheme.id
		    FROM Template t
		    WHERE t.owner.id = :memberId
		    ORDER BY t.updatedAt DESC
		""")
	List<Long> findRecentThemeIdsByMemberId(@Param("memberId") Long memberId, Pageable pageable);

	@Query("""
		    select t 
			from Template t
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

	@Query("""
        SELECT t
        FROM Template t
            LEFT JOIN FETCH t.templateCities tc
            LEFT JOIN FETCH tc.city c
            LEFT JOIN FETCH c.region r
        WHERE t.owner.id = :ownerId
        ORDER BY t.updatedAt DESC
    """)
	List<Template> findRecentTemplatesByOwner(Long ownerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Template t set t.owner.id = :deletedMemberId where t.owner.id = :memberId")
    void transferOwner(@Param("memberId") Long memberId,
                       @Param("deletedMemberId") Long deletedMemberId);

}
