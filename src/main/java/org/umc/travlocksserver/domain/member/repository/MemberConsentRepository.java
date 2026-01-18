package org.umc.travlocksserver.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.travlocksserver.domain.member.entity.mapping.MemberConsent;

public interface MemberConsentRepository extends JpaRepository<MemberConsent, Long> {
}
