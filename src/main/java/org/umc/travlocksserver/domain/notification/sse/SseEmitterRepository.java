package org.umc.travlocksserver.domain.notification.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// ✨살아있는 SSE 연결(emitter)을 member 기준으로 메모리에서 보관/조회/관리하는 클래스
@Component
public class SseEmitterRepository {

    // emitter을 저장하는 Map
    private final Map<Long, Map<String, SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * 새 SSE 연결을 등록
    * */
    public void add(Long memberId, String emitterId, SseEmitter emitter) {
        emitters.computeIfAbsent(memberId, k -> new ConcurrentHashMap<>()).put(emitterId, emitter);
    }

    /**
     * 해당 멤버의 전체 SSE 연결 조회
     * */
    public Map<String, SseEmitter> getEmitters(Long memberId) {
        return emitters.getOrDefault(memberId, Map.of());
    }

    /**
     * 해당 emitter 연결 제거
     * */
    public void remove(Long memberId, String emitterId) {
        Map<String, SseEmitter> memberEmitters = emitters.get(memberId);
        if (memberEmitters == null) return;
        memberEmitters.remove(emitterId);

        // member emitter 없는 경우 member 자체 제거
        if (memberEmitters.isEmpty()) {
            emitters.remove(memberId);
        }
    }

    /**
     * 해당 멤버의 전체 SSE 연결 제거
     * */
    public void removeAll(Long memberId) {
        emitters.remove(memberId);
    }

    public Map<Long, Map<String, SseEmitter>> getAll() {
        return emitters;
    }
}
