package org.sav.cardsback.dto;

import lombok.*;

@Builder
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StateLimitDto {

	WordStateDto state;
	Integer attempt;
	Integer delay;
	String color;

	public StateLimitDto(Integer stateId, Integer attempt, Integer delay, String color) {
		this.state = WordStateDto.fromId(stateId);
		this.attempt = attempt;
		this.delay = delay;
		this.color = color;
	}
}
