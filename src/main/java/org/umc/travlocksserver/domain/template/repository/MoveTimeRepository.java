package org.umc.travlocksserver.domain.template.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.travlocksserver.domain.template.entity.MoveTime;
import org.umc.travlocksserver.domain.template.enums.TransportType;

public interface MoveTimeRepository extends JpaRepository<MoveTime, Long> {
	Optional<MoveTime> findByFromVlockIdAndToVlockIdAndTransportType(
		Long fromVlockId,
		Long toVlockId,
		TransportType transportType
	);
}