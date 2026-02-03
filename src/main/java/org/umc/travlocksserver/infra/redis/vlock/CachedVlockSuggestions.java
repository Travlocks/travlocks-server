package org.umc.travlocksserver.infra.redis.vlock;

import java.util.ArrayList;
import java.util.List;

public record CachedVlockSuggestions(
        List<Long> vlockIds,
        List<Long> recentPickedIds
) {
    // ⚪ 새 추천 풀을 만드는 정적 팩토리 메서드
    public static CachedVlockSuggestions of(List<Long> vlockIds) {
        return new CachedVlockSuggestions(vlockIds, new ArrayList<>());
    }

    // ⚪ 기존 풀에서 recent만 업데이트한 새 풀을 생성하는 정적 팩토리 메서드
    public CachedVlockSuggestions withRecentPickedIds(List<Long> recentPickedIds) {
        return new CachedVlockSuggestions(vlockIds, recentPickedIds);
    }
}
