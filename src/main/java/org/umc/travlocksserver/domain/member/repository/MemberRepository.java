package org.umc.travlocksserver.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.travlocksserver.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);
}
