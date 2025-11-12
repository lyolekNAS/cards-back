package org.sav.cardsback.dto;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class StatisticAttemptDto extends StatisticComonDto{
	StatisticAttemptDto(int stateId, Long count, Long englishCnt, Long ukrainianCnt){
		super(stateId, count);
		this.englishCnt = englishCnt;
		this.ukrainianCnt = ukrainianCnt;
	}
	Long englishCnt;
	Long ukrainianCnt;
}
