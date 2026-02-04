package org.umc.travlocksserver.domain.vlock.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.member.dto.response.CreatedVlockDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberMyPageResponseDTO;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;

import java.util.List;
import java.util.Optional;

@Repository
public interface VlockRepository extends JpaRepository<Vlock,Long>, VlockRepositoryCustom {

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
            @Param("cityIds") List<Long> cityId,
            Pageable pageable
    );

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
            @Param("cityIds") List<Long> cityIds,
            @Param("excludeIds") List<Long> excludeVlockIds,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng,
            Pageable pageable
    );

    Optional<Vlock> findByExternalPlaceIdAndIsPublicTrue(String externalPlaceId);

    @EntityGraph(attributePaths = {"vlockCategory", "city", "city.region"})
    List<Vlock> findAllByOwnerIdAndCityIdAndDeletedAtIsNullOrderByUsageCountDescIdDesc(Long ownerId, Long cityId);

	Optional<Vlock> findByIdAndDeletedAtIsNull(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Vlock v set v.owner.id = :deletedMemberId where v.owner.id = :memberId")
    void transferOwner(@Param("memberId") Long memberId,
                       @Param("deletedMemberId") Long deletedMemberId);

    @Query("""
    select new org.umc.travlocksserver.domain.member.dto.response.CreatedVlockDTO(
        v.id,
        v.name,
        v.city.name,
        v.createdAt
    )
    from Vlock v
    where v.owner.id = :memberId
      and v.deletedAt is null
    order by v.createdAt desc, v.id desc
    """)
    List<CreatedVlockDTO> findRecentCreatedVlocksInternal(
            @Param("memberId") Long memberId
    );
    default List<CreatedVlockDTO> findRecentCreatedVlocks(Long memberId, int limit) {
        List<CreatedVlockDTO> all = findRecentCreatedVlocksInternal(memberId);
        return all.size() > limit ? all.subList(0, limit) : all;
    }

}
