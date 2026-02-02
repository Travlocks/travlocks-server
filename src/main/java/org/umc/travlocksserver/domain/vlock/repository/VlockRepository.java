package org.umc.travlocksserver.domain.vlock.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;

import java.util.List;

@Repository
public interface VlockRepository extends JpaRepository<Vlock,Long> {

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
}
