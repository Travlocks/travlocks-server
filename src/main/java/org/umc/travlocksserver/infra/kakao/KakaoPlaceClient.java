package org.umc.travlocksserver.infra.kakao;

import java.util.List;

// ✨ 카카오 지도 API로부터 장소 정보를 가져오는 기능을 정의한 인터페이스
public interface KakaoPlaceClient {

    List<KakaoPlace> searchPlaces(String query, Double x, Double y, Integer radius, int size);
}
