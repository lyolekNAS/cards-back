package org.sav.cardsback.dto;

import lombok.Data;

@Data
public class TrainedWordDto {
	Long id;
	boolean success;
	WordLangDto lang;

}
