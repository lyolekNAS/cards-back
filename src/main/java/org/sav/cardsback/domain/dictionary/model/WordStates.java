package org.sav.cardsback.domain.dictionary.model;

import lombok.Getter;

@Getter
public enum WordStates {
	FAKE		((int) Math.pow(2,  0)),
	DATAMUSE_ML		((int) Math.pow(2,  1)),
	DATAMUSE_FOUND	((int) Math.pow(2,  2)),
	TRANS_AZURE		((int) Math.pow(2,  3)),
	MERR_WEBSTER		((int) Math.pow(2,  4));

	private final int id;

	WordStates(int id){
		this.id = id;
	}
}
