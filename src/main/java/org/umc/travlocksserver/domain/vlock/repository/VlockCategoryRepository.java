package org.umc.travlocksserver.domain.vlock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.vlock.entity.VlockCategory;

@Repository
public interface VlockCategoryRepository extends JpaRepository<VlockCategory, Long> {
}
