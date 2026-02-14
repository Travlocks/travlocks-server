package org.umc.travlocksserver.global.profile;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DefaultProfileImageProvider {

	private final String baseUrl;
	private final List<String> files;

	public DefaultProfileImageProvider(DefaultProfileImageProperties props) {
		this.baseUrl = normalizeBaseUrl(props.defaultBaseUrl());
		this.files = props.defaultFiles();

		if (files == null || files.isEmpty()) {
			throw new IllegalStateException("기본 프로필 이미지 파일 목록이 비어있습니다.");
		}
	}

	public String pickRandomUrl() {
		int idx = ThreadLocalRandom.current().nextInt(files.size());
		return baseUrl + "/" + files.get(idx);
	}

	private String normalizeBaseUrl(String url) {
		if (url == null || url.isBlank()) {
			throw new IllegalStateException("기본 프로필 이미지 base url이 비어있습니다.");
		}
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}
}
