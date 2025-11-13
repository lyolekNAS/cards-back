package org.sav.cardsback.domain.dictionary.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public enum PartOfSpeech {
	NOUN("n", "Noun"),
	VERB("v", "Verb"),
	ADJECTIVE("adj", "Adjective"),
	ADVERB("adv", "Adverb"),
	PRONOUN("pron", "Pronoun"),
	PREPOSITION("prep", "Preposition"),
	CONJUNCTION("conj", "Conjunction"),
	INTERJECTION("intj", "Interjection"),
	ARTICLE("art", "Article"),
	AUXILIARY("aux", "Auxiliary-verb"),
	ABBREVIATION("abbr", "Abbreviation");

	private final String code;
	private final String displayName;

	private static final Map<String, PartOfSpeech> CODE_LOOKUP;
	private static final Map<String, PartOfSpeech> NAME_LOOKUP;

	static {
		CODE_LOOKUP = Arrays.stream(values())
				.filter(p -> p.code != null && !p.code.isEmpty())
				.collect(Collectors.toMap(p -> p.code.toLowerCase(Locale.ROOT), p -> p));
		NAME_LOOKUP = Arrays.stream(values())
				.collect(Collectors.toMap(p -> p.displayName.toLowerCase(Locale.ROOT), p -> p));
	}

	PartOfSpeech(String code, String displayName) {
		this.code = code;
		this.displayName = displayName;
	}

	public static boolean isValid(String name) {
		if(name == null)
			return false;
		return NAME_LOOKUP.get(name.toLowerCase(Locale.ROOT)) != null;
	}

	public static String getAllDisplayNamesLowercase() {
		return Arrays.stream(values())
				.map(p -> p.displayName.toLowerCase(Locale.ROOT))
				.collect(Collectors.joining(","));
	}
}
