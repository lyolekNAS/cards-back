package org.sav.cardsback.domain.dictionary.model;

import lombok.Getter;

@Getter
public enum WordStates {
	FAKE				((int) Math.pow(2,  0)), // 1
	WITH_EXAMPLES		((int) Math.pow(2,  1)), // 2
	AI_TRANSLATED		((int) Math.pow(2,  2)), // 4
	UNUSED_3			((int) Math.pow(2,  3)), // 8
	MERR_WEBSTER		((int) Math.pow(2,  4)); // 16

	private final int id;

	WordStates(int id){
		this.id = id;
	}
}
