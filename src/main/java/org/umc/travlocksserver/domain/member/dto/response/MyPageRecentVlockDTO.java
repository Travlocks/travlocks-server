package org.umc.travlocksserver.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MyPageRecentVlockDTO {
    private Long vlockId;
    private String vlockName;
    private Long regionId;
    private LocalDateTime createdAt;
}

