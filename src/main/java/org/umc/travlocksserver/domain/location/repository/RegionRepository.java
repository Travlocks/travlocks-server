package org.umc.travlocksserver.domain.location.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.location.entity.Region;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

}
