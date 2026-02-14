package org.umc.travlocksserver.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MyPageRecentTemplateDTO {
	private Long templateId;
	private String templateTitle;
	private Long regionId;
	private LocalDateTime createdAt;
	private boolean isFavorite;
}
