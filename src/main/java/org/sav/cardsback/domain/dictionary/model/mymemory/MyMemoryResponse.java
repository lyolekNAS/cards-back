package org.sav.cardsback.domain.dictionary.model.mymemory;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MyMemoryResponse {
	@JsonProperty("responseData")
	private ResponseData responseData;

	@JsonProperty("quotaFinished")
	private boolean quotaFinished;

	@JsonProperty("matches")
	private List<MatchItem> matches;
}
