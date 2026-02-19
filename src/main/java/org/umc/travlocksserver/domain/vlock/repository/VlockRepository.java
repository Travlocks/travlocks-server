package org.umc.travlocksserver.domain.vlock.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.member.dto.response.MyPageRecentVlockDTO;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;

import java.util.List;
import java.util.Optional;

@Repository
public interface VlockRepository extends JpaRepository<Vlock, Long>, VlockRepositoryCustom {

	@Query("""
		    SELECT v
		    FROM Vlock v
		        JOIN FETCH v.vlockCategory vc
		    WHERE v.city.id IN :cityIds
		        AND v.isPublic = true
		        AND vc.name != "숙소"
		    ORDER BY v.usageCount DESC
		""")
	List<Vlock> findPopularByCityIds(
		@Param("cityIds")
		List<Long> cityId,
		Pageable pageable);

	@Query("""
		    SELECT v
		    FROM Vlock v
		        JOIN FETCH v.vlockCategory vc
		    WHERE v.city.id IN :cityIds
		        AND v.isPublic = true
		        AND vc.name != '숙소'
		        AND v.latitude BETWEEN :minLat AND :maxLat
		        AND v.longitude BETWEEN :minLng AND :maxLng
		    ORDER BY v.usageCount DESC
		""")
	List<Vlock> findVlocksInBoxExcluding(
		@Param("cityIds")
		List<Long> cityIds,
		@Param("excludeIds")
		List<Long> excludeVlockIds,
		@Param("minLat")
		double minLat,
		@Param("maxLat")
		double maxLat,
		@Param("minLng")
		double minLng,
		@Param("maxLng")
		double maxLng,
		Pageable pageable);

	@EntityGraph(attributePaths = {"vlockCategory", "city", "city.region"})
	List<Vlock> findAllByOwnerIdAndCityIdAndDeletedAtIsNullOrderByUsageCountDescIdDesc(Long ownerId, Long cityId);

	Optional<Vlock> findByIdAndDeletedAtIsNull(Long id);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("update Vlock v set v.owner.id = :deletedMemberId where v.owner.id = :memberId")
	void transferOwner(@Param("memberId")
	Long memberId,
		@Param("deletedMemberId")
		Long deletedMemberId);

	@Query("""
		select new org.umc.travlocksserver.domain.member.dto.response.MyPageRecentVlockDTO(
		    v.id,
		    v.name,
		    v.city.region.id,
		    v.createdAt
		)
		from Vlock v
		where v.owner.id = :memberId
		  and v.deletedAt is null
		order by v.createdAt desc, v.id desc
		""")
	List<MyPageRecentVlockDTO> findRecentCreatedVlocks(
		@Param("memberId")
		Long memberId,
		Pageable pageable);

	@Query("""
		    SELECT v
		    FROM Vlock v
		    WHERE (v.name LIKE %:keyword% OR v.address LIKE %:keyword%)
		      AND v.isPublic = true
		      AND v.deletedAt IS NULL
		""")
	List<Vlock> searchByKeyword(@Param("keyword")
	String keyword, Pageable pageable);

//	boolean existsByExternalPlaceIdAndIsPublicTrue(String externalPlaceId);
}
