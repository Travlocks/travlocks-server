package org.umc.travlocksserver.infra.kakao;

public record KakaoPlace(
        String placeId,
        String name,
        String address,
        Double latitude,
        Double longitude,
        String categoryName,
        String placeUrl
) {
    public static KakaoPlace from(KakaoSearchResponseDTO.KakaoDocument document) {
        Double lat = safeParse(document.y());
        Double lng = safeParse(document.x());

        String address = document.roadAddressName() != null
                ? document.roadAddressName()
                : document.addressName();

        return new KakaoPlace(
                document.id(),
                document.placeName(),
                address,
                lat,
                lng,
                document.categoryGroupName(),
                document.placeUrl()
        );
    }

    private static Double safeParse(String s) {
        try {
            return s == null ? null : Double.parseDouble(s);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }
}
