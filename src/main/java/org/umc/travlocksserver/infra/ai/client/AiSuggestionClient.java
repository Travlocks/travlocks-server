package org.umc.travlocksserver.infra.ai.client;

import org.umc.travlocksserver.domain.vlock.entity.Vlock;

import java.util.List;
import java.util.Map;

public interface AiSuggestionClient {

	Map<Long, Double> suggestVlocks(List<Vlock> usedVlocksInTemplate, List<Vlock> candidates);
}
