package org.umc.travlocksserver.domain.template.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.travlocksserver.domain.template.entity.MoveTime;
import org.umc.travlocksserver.domain.template.enums.TransportType;

public interface MoveTimeRepository extends JpaRepository<MoveTime, Long> {
	@Query("""
		SELECT m FROM MoveTime m
		WHERE m.fromVlock.id = :fromId
		  AND m.toVlock.id = :toId
		  AND m.transportType = :type
		  AND (m.expiresAt IS NULL OR m.expiresAt > :now)
		""")
	Optional<MoveTime> findValidRoute(
		@Param("fromId") Long fromId,
		@Param("toId") Long toId,
		@Param("type") TransportType type,
		@Param("now") LocalDateTime now);

	@Query("""
		select mt
		from MoveTime mt
		join mt.fromVlock fv
		join mt.toVlock tv
		where mt.transportType = :transportType
		  and abs(fv.latitude - :fromLat) <= :latTolerance
		  and abs(fv.longitude - :fromLon) <= :lonTolerance
		  and abs(tv.latitude - :toLat) <= :latTolerance
		  and abs(tv.longitude - :toLon) <= :lonTolerance
		order by mt.id asc
		limit 1
		""")
	Optional<MoveTime> findTopReusableNearbyRoute(
		@Param("fromLat") Double fromLat,
		@Param("fromLon") Double fromLon,
		@Param("toLat") Double toLat,
		@Param("toLon") Double toLon,
		@Param("latTolerance") double latTolerance,
		@Param("lonTolerance") double lonTolerance,
		@Param("transportType") TransportType transportType);

	@Query("""
		select mt
		from MoveTime mt
		join mt.fromVlock fv
		join mt.toVlock tv
		where mt.transportType = :transportType
		  and abs(fv.latitude - :toLat) <= :latTolerance
		  and abs(fv.longitude - :toLon) <= :lonTolerance
		  and abs(tv.latitude - :fromLat) <= :latTolerance
		  and abs(tv.longitude - :fromLon) <= :lonTolerance
		order by mt.id asc
		limit 1
		""")
	Optional<MoveTime> findTopReusableNearbyRouteReverse(
		@Param("fromLat") Double fromLat,
		@Param("fromLon") Double fromLon,
		@Param("toLat") Double toLat,
		@Param("toLon") Double toLon,
		@Param("latTolerance") double latTolerance,
		@Param("lonTolerance") double lonTolerance,
		@Param("transportType") TransportType transportType);
}
