package org.sav.cardsback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StatisticDto {
	List<StatisticAttemptDto> statisticsAttemptDto;
	List<StatisticComonDto> statisticsComonDto;
	long totalCommonCount;
	long totalAttemptCount;
	long totalAttemptSum;
}
