package org.umc.travlocksserver.global.response;

import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

@Builder
public record PageResponseDTO<T>(
	List<T> content,

	int page,
	int size,

	long totalElements,
	int totalPages,

	boolean first,
	boolean last,
	boolean empty) {
	public static <T> PageResponseDTO<T> from(Page<T> page) {
		return PageResponseDTO.<T>builder()
			.content(page.getContent())
			.page(page.getNumber())
			.size(page.getSize())
			.totalElements(page.getTotalElements())
			.totalPages(page.getTotalPages())
			.first(page.isFirst())
			.last(page.isLast())
			.empty(page.isEmpty())
			.build();
	}
}
