package org.umc.travlocksserver.domain.template.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TripDays {

	ONE_DAY(1, "당일치기"),
	TWO_DAYS(2, "1박 2일"),
	THREE_DAYS(3, "2박 3일"),
	FOUR_DAYS(4, "3박 4일"),
	FIVE_DAYS(5, "4박 5일");

	private final int days;
	private final String description;
}
