package org.sav.cardsback.dto;

import lombok.Getter;
import org.sav.cardsback.entity.WordState;

@Getter
public enum WordStateDto {
	PAUSED(0),
	STAGE_1(1),
	STAGE_2(2),
	STAGE_3(3),
	STAGE_4(4),
	STAGE_5(5),
	STAGE_6(6),
	STAGE_7(7),
	DONE(10);
	
	private final int id;

	WordStateDto(int id){
		this.id = id;
	}

	public static WordStateDto fromId(int id) {
		for (WordStateDto state : WordStateDto.values()) {
			if (state.getId() == id) {
				return state;
			}
		}
		throw new IllegalArgumentException("No enum constant with code " + id);
	}

	public static WordState fromName(String stateName) {
		for (WordStateDto state : WordStateDto.values()) {
			if (state.name().equalsIgnoreCase(stateName)) {
				return new WordState(state.getId());
			}
		}
		throw new IllegalArgumentException("No enum constant with name " + stateName);
	}
}