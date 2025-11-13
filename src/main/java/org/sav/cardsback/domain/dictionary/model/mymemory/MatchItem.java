package org.sav.cardsback.domain.dictionary.model.mymemory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class MatchItem {
	private long id;
	private String segment;
	private String translation;
	private String source;
	private String target;
	private String quality;
	private String reference;

	// subject може бути boolean або string -> Object
	private Object subject;

	@JsonProperty("usage-count")
	private Integer usageCount;

	private Double match;

	@JsonProperty("created-by")
	private String createdBy;

	@JsonProperty("last-updated-by")
	private String lastUpdatedBy;

	@JsonProperty("create-date")
	private String createDate;

	@JsonProperty("last-update-date")
	private String lastUpdateDate;

	private String penalty;
	private String model;
}
