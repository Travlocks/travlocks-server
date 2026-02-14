package org.umc.travlocksserver.infra.ai;

import org.umc.travlocksserver.domain.vlock.entity.Vlock;

public record ScoredCandidate(
	Vlock vlock,
	Double score) {
	public static ScoredCandidate of(Vlock vlock, Double score) {
		return new ScoredCandidate(vlock, score);
	}
}
