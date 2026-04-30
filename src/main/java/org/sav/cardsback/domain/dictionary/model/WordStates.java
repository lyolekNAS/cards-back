package org.sav.cardsback.domain.dictionary.model;

import lombok.Getter;

@Getter
public enum WordStates {
	FAKE				((int) Math.pow(2,  0)),
	WITH_EXAMPLES		((int) Math.pow(2,  1)),
	UNUSED_2			((int) Math.pow(2,  2)),
	UNUSED_3			((int) Math.pow(2,  3)),
	MERR_WEBSTER		((int) Math.pow(2,  4));

	private final int id;

	WordStates(int id){
		this.id = id;
	}
}
