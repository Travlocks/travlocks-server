package org.umc.travlocksserver.domain.template.dto.response;

import org.umc.travlocksserver.domain.template.entity.TemplateVlock;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;

import java.util.List;

public record OptimizeResponseDTO(
        Long templateDayId,
        List<OptimizedVlockResponseDTO> vlocks,
        Double distance
) {
    public record OptimizedVlockResponseDTO(
            Long templateVlockId,
            Integer orderNo,
            String stayTimes,
            Long vlockId,
            String vlockName,
            String categoryName
    ) {
        public static OptimizedVlockResponseDTO from(TemplateVlock tv) {
            Vlock vlock = tv.getVlock();
            String categoryName = vlock.getVlockCategory().getName();

            String stayTimes;
            if ("숙소".equals(categoryName)) {
                stayTimes = "1박";
            } else {
                double hour = tv.getStayHours();

                if (hour >= 1 && hour == Math.floor(hour)) {
                    stayTimes = (int) hour + "시간";
                } else {
                    stayTimes = (int) (hour * 60) + "분";
                }
            }

            return new OptimizedVlockResponseDTO(
                    tv.getId(),
                    tv.getOrderNo(),
                    stayTimes,
                    vlock.getId(),
                    vlock.getName(),
                    categoryName
            );
        }
    }

    public static OptimizeResponseDTO from(Long templateDayId, List<TemplateVlock> vlocks, Double distance) {
        List<OptimizedVlockResponseDTO> mapped = vlocks.stream()
                .map(OptimizedVlockResponseDTO::from)
                .toList();
        return new OptimizeResponseDTO(templateDayId, mapped, distance);
    }
}
