package org.umc.travlocksserver.domain.template.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.umc.travlocksserver.domain.template.entity.TemplateRating;

public interface TemplateRatingRepository extends JpaRepository<TemplateRating, Long> {

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("update TemplateRating tr set tr.member.id = :deletedMemberId where tr.member.id = :memberId")
	void transferRater(@Param("memberId")
	Long memberId,
		@Param("deletedMemberId")
		Long deletedMemberId);

	@Query("select coalesce(avg(tr.rating), 0.0) from TemplateRating tr where tr.template.id = :templateId")
	Double findAvgRatingByTemplateId(@Param("templateId")
	Long templateId);

	boolean existsByTemplateIdAndMemberId(Long templateId, Long memberId);
}
