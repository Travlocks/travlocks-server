package org.umc.travlocksserver.domain.vlock.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;

import java.util.List;
import java.util.Optional;

@Repository
public interface VlockRepository extends JpaRepository<Vlock,Long>, VlockRepositoryCustom {

    @EntityGraph(attributePaths = {"vlockCategory", "city", "city.region"})
    List<Vlock> findAllByOwnerIdAndCityIdAndDeletedAtIsNullOrderByUsageCountDescIdDesc(Long ownerId, Long cityId);

	Optional<Vlock> findByIdAndDeletedAtIsNull(Long id);
}
