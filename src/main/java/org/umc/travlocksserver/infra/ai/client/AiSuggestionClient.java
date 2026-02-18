package org.umc.travlocksserver.infra.ai.client;

import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.infra.ai.dto.AiTagResponseDTO;

import java.util.List;
import java.util.Map;

public interface AiSuggestionClient {

	public Map<Long, Double> suggestVlocks(List<Vlock> usedVlocksInTemplate, List<Vlock> candidates);

	AiTagResponseDTO generateTags(
			String region,
			List<String> fixedTags,
			List<String> cityCandidates,
			List<Vlock> vlocksInTemplate
	);
}
