package org.sav.cardsback.domain.dictionary.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public enum PartOfSpeech {
	NOUN("n", "Noun", true),
	VERB("v", "Verb", true),
	ADJECTIVE("adj", "Adjective", true),
	ADVERB("adv", "Adverb", true),
	PRONOUN("pron", "Pronoun", true),
	PREPOSITION("prep", "Preposition", true),
	CONJUNCTION("conj", "Conjunction", true),
	INTERJECTION("intj", "Interjection", true),
	ARTICLE("art", "Article", true),
	AUXILIARY("aux", "Auxiliary-verb", true),
	PHRASE("phrase", "Phrase", false);

	private final String code;
	private final String displayName;
	private final boolean includeToWordnik;

	private static final Map<String, PartOfSpeech> CODE_LOOKUP;
	private static final Map<String, PartOfSpeech> NAME_LOOKUP;

	static {
		CODE_LOOKUP = Arrays.stream(values())
				.filter(p -> p.code != null && !p.code.isEmpty())
				.collect(Collectors.toMap(p -> p.code.toLowerCase(Locale.ROOT), p -> p));
		NAME_LOOKUP = Arrays.stream(values())
				.collect(Collectors.toMap(p -> p.displayName.toLowerCase(Locale.ROOT), p -> p));
	}

	PartOfSpeech(String code, String displayName, boolean includeToWordnik) {
		this.code = code;
		this.displayName = displayName;
		this.includeToWordnik = includeToWordnik;
	}

	public static boolean isValid(String name) {
		if(name == null)
			return false;
		return NAME_LOOKUP.get(name.toLowerCase(Locale.ROOT)) != null;
	}

	public static String getAllDisplayNamesLowercase() {
		return Arrays.stream(values())
				.filter(p -> p.includeToWordnik)
				.map(p -> p.displayName.toLowerCase(Locale.ROOT))
				.collect(Collectors.joining(","));
	}
}
