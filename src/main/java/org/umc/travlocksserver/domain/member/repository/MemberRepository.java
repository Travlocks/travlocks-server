package org.umc.travlocksserver.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.umc.travlocksserver.domain.member.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);

	boolean existsByNicknameAndIdNot(String nickname, Long id);

	Optional<Member> findByEmail(String email);

	@Modifying
	@Query("""
        UPDATE Member m
        SET m.notificationCount = m.notificationCount + 1
        WHERE m.id = :memberId
        """)
	void increaseNotificationCount(Long memberId);
}
