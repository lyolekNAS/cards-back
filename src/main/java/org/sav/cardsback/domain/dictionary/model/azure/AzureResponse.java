package org.sav.cardsback.domain.dictionary.model.azure;


import lombok.Data;
import java.util.List;

@Data
public class AzureResponse {
	private String normalizedSource;
	private String displaySource;
	private List<AzureTranslation> translations;
}