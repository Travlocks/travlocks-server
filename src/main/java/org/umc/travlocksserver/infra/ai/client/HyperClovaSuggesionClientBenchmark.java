package org.umc.travlocksserver.infra.ai.client;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.infra.ai.dto.AiRequestDTO;
import org.umc.travlocksserver.infra.ai.dto.AiTagResponseDTO;
import org.umc.travlocksserver.infra.ai.util.AiPromptProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Profile("benchmark")
public class HyperClovaSuggesionClientBenchmark implements AiSuggestionClient {

    private final AiPromptProvider aiPromptProvider;

    @Override
    public AiTagResponseDTO generateTags(
            String region,
            List<String> fixedTags,
            List<String> cityCandidates,
            List<Vlock> vlocksInTemplate
    ) {
        String systemPrompt = aiPromptProvider.getSystemPromptForTagGeneration();
        String userPrompt = aiPromptProvider.buildUserPromptForTagGeneration(region, fixedTags, cityCandidates, vlocksInTemplate);
        AiRequestDTO request = AiRequestDTO.of(systemPrompt, userPrompt);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return AiTagResponseDTO.of(Collections.emptyList(), List.of("바다뷰", "힐링"));
    }

    @Override
    public Map<Long, Double> suggestVlocks(List<Vlock> usedVlocksInTemplate, List<Vlock> candidates) {
        // 아래는 더미 데이터
        return Map.of(
                1L, 0.9,
                2L, 0.8,
                3L, 0.7
        );
    }
}
