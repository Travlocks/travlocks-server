package org.umc.travlocksserver.domain.vlock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;

@Repository
public interface VlockRepository extends JpaRepository<Vlock,Long> {
}
