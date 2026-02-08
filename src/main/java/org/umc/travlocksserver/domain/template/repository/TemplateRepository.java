package org.umc.travlocksserver.domain.template.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.location.entity.Region;
import org.umc.travlocksserver.domain.member.dto.response.CreatedTemplateDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateCardResponseDTO;
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

	@Query("""
		SELECT t.id
		FROM Template t
		WHERE t.updatedAt between :from and :to
	""")
	List<Long> findRecentlyUpdatedTemplateIds(
			LocalDateTime from,
			LocalDateTime to
	);

	@Query("""
		SELECT r
		FROM Template t
			JOIN t.templateCities tc
			JOIN tc.city c
			JOIN c.region r
		WHERE t.id = :templateId
	""")
	List<Region> findRegionByTemplateId(Long templateId);

    @Query("""
    select new org.umc.travlocksserver.domain.member.dto.response.CreatedTemplateDTO(
        t.id,
        t.title,
        min(r.id),
        t.createdAt,
        case when count(f.id) > 0 then true else false end
    )
    from Template t
    left join t.templateCities tc
    left join tc.city c
    left join c.region r
    left join Favorite f
        on f.template.id = t.id
       and f.member.id = :memberId
    where t.owner.id = :memberId
      and t.deletedAt is null
    group by t.id, t.title, t.createdAt
    order by t.createdAt desc, t.id desc
    """)
    List<CreatedTemplateDTO> findRecentCreatedTemplatesInternalwithFavorite(
            @Param("memberId") Long memberId
    );
    default List<CreatedTemplateDTO> findRecentCreatedTemplates(Long memberId, int limit) {
        List<CreatedTemplateDTO> all = findRecentCreatedTemplatesInternalwithFavorite(memberId);
        return all.size() > limit ? all.subList(0, limit) : all;
    }

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
		FROM Template t
			JOIN t.owner o
			JOIN t.travelTheme tt
		WHERE t.owner.id = :memberId
		ORDER BY t.updatedAt DESC, t.id DESC
	""")
	Page<TemplateCardResponseDTO> findMyTemplates(Long memberId, Pageable pageable);
}
