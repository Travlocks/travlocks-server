package org.umc.travlocksserver.domain.location.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.location.entity.City;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

	@EntityGraph(attributePaths = {"region"})
	Optional<City> findWithRegionById(Long id);

	@Query("""
			SELECT c.name
			FROM City c
			WHERE c.region.id = :regionId
		""")
	List<String> findNameByRegionId(Long regionId);
}
