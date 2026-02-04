package org.umc.travlocksserver.domain.template.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.travlocksserver.domain.template.dto.response.OptimizeResponseDTO;
import org.umc.travlocksserver.domain.template.entity.TemplateDay;
import org.umc.travlocksserver.domain.template.entity.TemplateVlock;
import org.umc.travlocksserver.domain.template.repository.TemplateVlockRepository;
import org.umc.travlocksserver.domain.template.service.query.TemplateDayQueryService;
import org.umc.travlocksserver.domain.template.service.query.TemplateVlockQueryService;
import org.umc.travlocksserver.global.geo.GeoUtil;
import org.umc.travlocksserver.global.geo.LatLng;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemplateDayOptimizeCommandService {

    private final TemplateVlockRepository templateVlockRepository;
    private final TemplateDayQueryService templateDayQueryService;
    private final TemplateVlockQueryService templateVlockQueryService;

    public OptimizeResponseDTO optimizeVlocks(Long memberId, Long templateId, int dayNo) {
        TemplateDay templateDay = templateDayQueryService.getTemplateDayAccesible(memberId, templateId, dayNo);

        // 현재 TemplateDay 위의 vlock들을 순서대로 가져오기
        List<TemplateVlock> vlocks = templateVlockQueryService.getAllByTemplateDayIdOrderByOrderNo(templateDay.getId());

        if (vlocks.size() <= 1) {
            OptimizeResponseDTO.from(templateDay.getId(), vlocks, 0.0);
        }

        TemplateVlock start = vlocks.get(0);
        List<TemplateVlock> remaining = vlocks.subList(1, vlocks.size());

        Best best = optimize(start, remaining);

        // 최종 경로
        List<TemplateVlock> orderedVlocks = new ArrayList<>();
        orderedVlocks.add(start);
        orderedVlocks.addAll(best.vlocks);

        // orderNo 업데이트
        for (int i = 0; i < orderedVlocks.size(); i++) {
            orderedVlocks.get(i).updateOrderNo(i + 1);
        }

        templateVlockRepository.saveAll(orderedVlocks);

        return OptimizeResponseDTO.from(
                templateDay.getId(),
                orderedVlocks,
                best.distance
        );
    }

    private Best optimize(TemplateVlock start,List<TemplateVlock> remaining) {
        int n = remaining.size();

        int lunchIndex= pickIndex(n, true);
        int dinnerIndex = pickIndex(n, false);

        List<TemplateVlock> restaurants = remaining.stream()
                .filter(tv -> tv.getVlock().getVlockCategory().getName().equals("식당"))
                .toList();

        switch (restaurants.size()) {
            // 현재 블록들 중 식당이 없을 때
            case 0 -> {
                BestHolder holder = new BestHolder();
                List<TemplateVlock> arr = new ArrayList<>(remaining);

                backtrack(0, arr, perm -> {
                    double dist = totalDistanceMeter(start, perm);
                    if (holder.best == null || dist < holder.best.distance) {
                        holder.best = new Best(new ArrayList<>(perm), dist);
                    }
                });

                return holder.best;
            }

            // 현재 블록들 중 식당이 1개일 때 (점심/저녁 위치에 배치한 결과 비교)
            case 1 -> {
                TemplateVlock r = restaurants.get(0);
                // 식당을 점심 자리에 고정하고, 나머지를 거리 기준으로 최적 배치한 결과
                Best a = bestWithFixedPositions(start, remaining, Map.of(lunchIndex, r));

                // 식당을 저녁 자리에 고정하고, 나머지를 거리 기준으로 최적 배치한 결과
                Best b = bestWithFixedPositions(start, remaining, Map.of(dinnerIndex, r));

                return (a.distance <= b.distance) ? a : b;
            }

            // 현재 블록들 중 식당이 2개일 때
            case 2 -> {
                TemplateVlock r1 = restaurants.get(0);
                TemplateVlock r2 = restaurants.get(1);

                Best a = bestWithFixedPositions(start, remaining, Map.of(lunchIndex, r1, dinnerIndex, r2));
                Best b = bestWithFixedPositions(start, remaining, Map.of(dinnerIndex, r2, lunchIndex, r1));

                return (a.distance <= b.distance) ? a : b;
            }

            // 현재 블록들 중 식당이 3개 이상일 때
            default -> {
                return bestWithOverTwoFixedPositions(start, remaining, restaurants, lunchIndex, dinnerIndex);
            }
        }
    }

    /**
     * [식당이 3개 이상 있을 때 최적 경로를 탐색하는 메서드]
     * 각 식당 쌍을 점심/저녁에 고정한 뒤 최적 경로를 계산해 그중 최소 거리 선택
     */
    private Best bestWithOverTwoFixedPositions(
            TemplateVlock start,
            List<TemplateVlock> remaining,
            List<TemplateVlock> restaurants,
            int lunchIdx,
            int dinnerIdx
    ) {
        BestHolder holder = new BestHolder();

        for (int i = 0; i < restaurants.size(); i++) {
            for (int j = 0; j < restaurants.size(); j++) {
                if (i == j) continue;

                TemplateVlock lunchR = restaurants.get(i);
                TemplateVlock dinnerR = restaurants.get(j);

                Best cand = bestWithFixedPositions(start, remaining, Map.of(lunchIdx, lunchR, dinnerIdx, dinnerR));
                if (holder.best == null || cand.distance < holder.best.distance) {
                    holder.best = cand;
                }
            }
        }
        return holder.best;
    }

    /**
     * [고정 위치(점심/저녁)이 있을 때 최적 경로를 탐색하는 메서드]
     * 출발지 제외 블록들에 대해 고정 위치는 고정하고, 나머지 블록들을 순열를 이용해 경로를 조립한 뒤 거리가 최소인 경로를 찾는다.
     */
    private Best bestWithFixedPositions(
            TemplateVlock start,
            List<TemplateVlock> remaining,
            Map<Integer, TemplateVlock> fixed
    ) {
        int n = remaining.size();

        // 고정된 식당들
        Set<Long> fixedTVIds = fixed.values().stream()
                .map(TemplateVlock::getId)
                .collect(Collectors.toSet());

        // 고정된 식당을 제외한 블록 리스트 만들기
        List<TemplateVlock> free = remaining.stream()
                .filter(tv -> !fixedTVIds.contains(tv.getId()))
                .collect(Collectors.toCollection(ArrayList::new));

        BestHolder holder = new BestHolder();

        // free를 permutation하며 완성 route를 구성해서 평가 (permFree: 고정안된 블록들의 순열)
        backtrack(0, free, permFree -> {
            List<TemplateVlock> route = buildRouteWithFixed(n, fixed, permFree);
            double dist = totalDistanceMeter(start, route);

            if (holder.best == null || dist < holder.best.distance) {
                holder.best = new Best(route, dist);
            }
        });

        return holder.best;
    }

    /**
     * [고정 위치를 두고 빈 자리들을 순열로 채워 경로를 완성하는 메서드]
     * */
    private List<TemplateVlock> buildRouteWithFixed(
            int n,
            Map<Integer, TemplateVlock> fixedPositions,
            List<TemplateVlock> permFree
    ) {
        List<TemplateVlock> route = new ArrayList<>(n);
        int freeIndex = 0;  // 몇번째를 꺼낼지

        for (int index = 0; index < n; index++) {
            // 해당 자리가 고정된 자리인지 확인하고, 고정이라면 해당 TemplateVlock 반환, 아니라면 null 반화
            TemplateVlock fixed = fixedPositions.get(index);
            if (fixed != null) route.add(fixed);
            else route.add(permFree.get(freeIndex++));
        }
        return route;
    }

    /**
     * [블록 수에 따라 점심, 저녁 식사 위치를 지정하는 메서드]
     * */
    private int pickIndex(int n, boolean lunch) {
        if (n <= 0)
            return 0;

        int index = lunch ? (int) Math.floor(n * 0.33) : (int) Math.floor(n * 0.66);

        return Math.max(0, Math.min(index, n - 1));
    }

    /**
     * [vlocks들의 가능한 모든 순서를 생성하는 메서드 (백트래킹 이용)]
     */
    private void backtrack(int fixedIndex, List<TemplateVlock> vlocks, Consumer<List<TemplateVlock>> consumer) {
        if (fixedIndex == vlocks.size()) {
            consumer.accept(new ArrayList<>(vlocks));
            return;
        }
        for (int i = fixedIndex; i < vlocks.size(); i++) {
            Collections.swap(vlocks, fixedIndex, i);
            backtrack(fixedIndex + 1, vlocks, consumer);
            Collections.swap(vlocks, fixedIndex, i);
        }
    }

    /**
     * [총 거리(각 블록의 직선거리 합)를 구하는 메서드]
     */
    private double totalDistanceMeter(TemplateVlock start, List<TemplateVlock> vlocks) {
        double sum = 0;
        TemplateVlock prev = start;

        for (TemplateVlock cur : vlocks) {
            sum += GeoUtil.haversineKm(
                    new LatLng(prev.getVlock().getLatitude(), prev.getVlock().getLongitude()),
                    new LatLng(cur.getVlock().getLatitude(), cur.getVlock().getLongitude())
            ) * 1000;
            prev = cur;
        }
        return sum;
    }

    private static class BestHolder {
        Best best;
    }

    public record Best(List<TemplateVlock> vlocks, double distance) {
        public static Best from(List<TemplateVlock> vlocks, double distance) {
            return new Best(vlocks, distance);
        }
    }
}
