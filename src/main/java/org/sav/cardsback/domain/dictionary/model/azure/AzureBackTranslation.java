package org.sav.cardsback.domain.dictionary.model.azure;

import lombok.Data;

@Data
public class AzureBackTranslation {
	private String normalizedText;
	private String displayText;
	private int numExamples;
	private int frequencyCount;
}
