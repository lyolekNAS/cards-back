package org.sav.cardsback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordDto {
	long id;
	String english;
	String ukrainian;
	String description;
	long userId;
	@Builder.Default
	int englishCnt = 0;
	@Builder.Default
	int ukrainianCnt = 0;
	OffsetDateTime lastTrain;
	OffsetDateTime nextTrain;
	WordLangDto lang;
	@Builder.Default
	WordStateDto state = WordStateDto.PAUSED;
	long dictWordId;
	long dictWordFreqSum;
	int rarity;
}
