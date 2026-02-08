package org.umc.travlocksserver.domain.member.dto.response;

import java.time.LocalDateTime;

public record CreatedVlockDTO(
        Long vlockId,
        String name,
        String city,
        String coverImgUrl,
        LocalDateTime createdAt
) {}

