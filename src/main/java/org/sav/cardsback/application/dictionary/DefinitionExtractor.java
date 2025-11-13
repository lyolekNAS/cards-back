package org.sav.cardsback.application.dictionary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.entity.DictWord;
import org.sav.cardsback.entity.DictWordDefinition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefinitionExtractor {

	public List<DictWordDefinition> createDefinitions(DictWord dictWord, List<Map.Entry<String, String>> defs) {
		return defs.stream()
				.distinct()
				.filter(entry -> dictWord.getDefinitions().stream()
						.noneMatch(existing ->
								existing.getPartOfSpeach().equalsIgnoreCase(entry.getKey()) &&
										existing.getDefinitionText().equalsIgnoreCase(entry.getValue())))
				.map(entry -> {
					DictWordDefinition def = new DictWordDefinition();
					def.setLemma(dictWord);
					def.setPartOfSpeach(entry.getKey());
					def.setDefinitionText(entry.getValue());
					return def;
				})
				.collect(Collectors.toCollection(ArrayList::new));
	}
}

