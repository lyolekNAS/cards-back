package org.sav.cardsback.application.merriamwebster;

import org.sav.cardsback.domain.dictionary.model.mw.MWSyn;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SynonymExtractor {

	private static final Pattern SC_PATTERN = Pattern.compile("\\{sc}(.*?)\\{/sc}");

	private SynonymExtractor() {}

	public static List<String> extractSynonymWords(List<MWSyn> syns) {
		List<String> result = new ArrayList<>();

		if (syns == null) return result;

		for (MWSyn syn : syns) {
			if (syn.getPt() == null) continue;

			for (List<Object> item : syn.getPt()) {
				if (item.size() >= 2 && "text".equals(item.get(0))) {
					String text = item.get(1).toString();
					Matcher matcher = SC_PATTERN.matcher(text);
					while (matcher.find()) {
						result.add(matcher.group(1));
					}
				}
			}
		}
		return result;
	}
}
