package org.sav.cardsback.application.merriamwebster;

import org.junit.jupiter.api.Test;
import org.sav.cardsback.domain.dictionary.model.mw.MWSyn;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SynonymExtractorTest {

	@Test
	void extractSynonymWords_whenInputIsNull_returnsEmptyList() {
		List<String> result = SynonymExtractor.extractSynonymWords(null);

		assertTrue(result.isEmpty());
	}

	@Test
	void extractSynonymWords_extractsWordsFromScTagsAcrossAllSynGroups() {
		MWSyn syn1 = new MWSyn();
		syn1.setPt(List.of(
				List.of("text", "some {sc}fast{/sc}, {sc}quick{/sc} words"),
				List.of("note", "ignored"),
				List.of("text", "{sc}rapid{/sc}")
		));

		MWSyn syn2 = new MWSyn();
		syn2.setPt(List.of(
				List.of("text", "another {sc}swift{/sc} synonym")
		));

		List<String> result = SynonymExtractor.extractSynonymWords(List.of(syn1, syn2));

		assertEquals(List.of("fast", "quick", "rapid", "swift"), result);
	}

	@Test
	void extractSynonymWords_ignoresNullPtAndInvalidItems() {
		MWSyn withNullPt = new MWSyn();
		withNullPt.setPt(null);

		MWSyn withInvalidItems = new MWSyn();
		withInvalidItems.setPt(List.of(
				List.of("text"), // size < 2
				List.of("not-text", "{sc}ignored{/sc}"),
				List.of("text", "plain text without tags")
		));

		List<String> result = SynonymExtractor.extractSynonymWords(List.of(withNullPt, withInvalidItems));

		assertTrue(result.isEmpty());
	}
}
