package org.umc.travlocksserver.domain.notification.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.enums.MemberStatus;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
import org.umc.travlocksserver.domain.notification.enums.NotificationType;
import org.umc.travlocksserver.domain.notification.repository.NotificationRepository;
import org.umc.travlocksserver.domain.notification.service.command.NotificationCommandService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class NotificationCommandServiceTest {

    @Autowired
    NotificationCommandService notificationCommandService;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TransactionTemplate transactionTemplate;

    private Member testReceiver;
    private Member testActor;

    @BeforeEach
    void setUp() {
        testReceiver = Member.builder()
                .nickname("receiver")
                .email("receiverEmail@travlocks.com")
                .status(MemberStatus.ACTIVE)
                .emailVerified(true)
                .vlockCount(0)
                .templateCount(0)
                .favoriteCount(0)
                .notificationCount(0)
                .build();
        memberRepository.save(testReceiver);

        testActor = Member.builder()
                .nickname("actor")
                .email("actorEmail@travlocks.com")
                .status(MemberStatus.ACTIVE)
                .emailVerified(true)
                .vlockCount(0)
                .templateCount(0)
                .favoriteCount(0)
                .notificationCount(0)
                .build();
        memberRepository.save(testActor);
    }

    @Test
    void notificationCountConcurrencyTest() throws Exception {
        int threadCount = 100;

        // 스레드풀 생성
        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    notificationCommandService.createNotification(testReceiver.getId(), testActor.getId(), 1L, NotificationType.TEMPLATE_REMIXED);
                } finally {
                    // 작업 완료를 latch에 알림
                    latch.countDown();
                }
            });
        }

        latch.await();  // 모든 스레드가 작업 마칠 때까지 대기
        executor.shutdown();

        testReceiver = memberRepository.findById(testReceiver.getId())
                        .orElseThrow();

        assertThat(testReceiver.getNotificationCount()).isEqualTo(threadCount);
    }

    @AfterEach
    void tearDown() {
        transactionTemplate.executeWithoutResult(status -> {
            notificationRepository.deleteAllByReceiverId(testReceiver.getId());
            memberRepository.deleteById(testActor.getId());
            memberRepository.deleteById(testReceiver.getId());
        });
    }
}
