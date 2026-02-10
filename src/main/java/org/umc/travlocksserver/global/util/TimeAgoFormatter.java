package org.umc.travlocksserver.global.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class TimeAgoFormatter {

    @Value("${global.timezone}")
    private String timezone;

    public String format(LocalDateTime createdAt) {
        ZoneId zoneId = ZoneId.of(timezone);

        if (createdAt == null) {
            return null;
        }

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime created = createdAt.atZone(zoneId);

        long minutes = ChronoUnit.MINUTES.between(created, now);
        if (minutes < 1) {
            return "방금 전";
        }

        if (minutes < 60) {
            return minutes + "분 전";
        }

        long hours = ChronoUnit.HOURS.between(created, now);
        if (hours < 24) {
            return hours + "시간 전";
        }

        long days = ChronoUnit.DAYS.between(created, now);
        if (days < 7) {
            return days + "일 전";
        }

        if (days < 30) {
            long weeks = days / 7;
            return weeks + "주 전";
        }

        if (days < 365) {
            long months = ChronoUnit.MONTHS.between(created, now);
            return months + "달 전";
        }

        long years = ChronoUnit.YEARS.between(created, now);
        return years + "년 전";
    }
}
