package org.sav.cardsback.domain.dictionary.model.mw;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MWSyn {

	private String pl;
	private List<List<Object>> pt;
}
