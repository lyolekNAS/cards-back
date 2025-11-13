package org.sav.cardsback.domain.dictionary.model.azure;


import lombok.Data;
import java.util.List;

@Data
public class AzureTranslation {
	private String normalizedTarget;
	private String displayTarget;
	private String posTag;
	private double confidence;
	private String prefixWord;
	private List<AzureBackTranslation> backTranslations;
}
