package org.sav.cardsback.domain.dictionary.model.mw;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MWMeta {
	private String id;
	private String uuid;
	private String sort;
	private String src;
	private String section;
	private List<String> stems;
	private boolean offensive;
}
