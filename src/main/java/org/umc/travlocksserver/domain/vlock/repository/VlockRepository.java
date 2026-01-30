package org.umc.travlocksserver.domain.vlock.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;

import java.util.List;

@Repository
public interface VlockRepository extends JpaRepository<Vlock,Long>, VlockRepositoryCustom {

    @EntityGraph(attributePaths = {"vlockCategory", "city", "city.region"})
    List<Vlock> findAllByOwnerIdAndCityIdAndDeletedAtIsNull(Long ownerId, Long cityId);
}
