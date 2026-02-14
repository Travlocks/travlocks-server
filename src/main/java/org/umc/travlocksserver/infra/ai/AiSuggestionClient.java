package org.umc.travlocksserver.infra.ai;

import org.umc.travlocksserver.domain.vlock.entity.Vlock;

import java.util.List;
import java.util.Map;

public interface AiSuggestionClient {

	Map<Long, Double> requestToAi(Long templateId, List<Vlock> usedVlocksInTemplate, List<Vlock> candidates);
}
