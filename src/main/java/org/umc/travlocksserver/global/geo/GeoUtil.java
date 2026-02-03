package org.umc.travlocksserver.global.geo;

// ✨위경도 기반 거리 계산 등을 제공하는 공간 유틸 클래스
public class GeoUtil {

    // ⚪ radiusKm 기준으로 bounding box를 계산하는 메서드
    public static BoundingBox box(LatLng center, double radiusKm) {
        double lat = center.lat();
        double lng = center.lng();

        // 1도 위도 = 111km
        double latDelta = radiusKm / 111.0;

        // 1도 경도 = 111km * cos(lat)
        double lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        return new BoundingBox(
                lat - latDelta, lat + latDelta,
                lng - lngDelta, lng + lngDelta
        );
    }

    // ⚪ 위경도 두 점 사이의 실제 지표면 거리를 계산하는 메서드 (지구가 둥글다는 것을 고려)
    public static double haversineKm(LatLng a, LatLng b) {
        final double R = 6371.0; // 지구 반경 km
        double dLat = Math.toRadians(b.lat() - a.lat());  // 위도 각도 차이를 라디안 단위로 변환 (삼각함수 사용을 위해)
        double dLng = Math.toRadians(b.lng() - a.lng());  // 경도 각도 차이를 라디안 단위로 변환 (삼각함수 사용을 위해)

        double sinLat = Math.sin(dLat / 2);
        double sinLng = Math.sin(dLng / 2);

        // 두 점이 지구 표면에서 얼마나 떨어져 있는지 계산
        double aa = sinLat * sinLat
                + Math.cos(Math.toRadians(a.lat())) * Math.cos(Math.toRadians(b.lat()))
                * sinLng * sinLng;

        double c = 2 * Math.atan2(Math.sqrt(aa), Math.sqrt(1 - aa));
        return R * c;  // 지구 반지름 *  두점을 잇는 각도 = 실제거리(km)
    }
}
