package org.sav.cardsback.domain.dictionary.model.mw;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MWEntry {
	private MWMeta meta;
	private String fl;
	private List<MWSyn> syns;
	@JsonProperty("shortdef")
	private List<String> shortDef;
}
