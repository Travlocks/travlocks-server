package org.umc.travlocksserver.infra.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.travlocksserver.infra.ai.entity.AiTagExecutionLog;

public interface AiTagExecutionLogRepository extends JpaRepository<AiTagExecutionLog, Long> {
}
