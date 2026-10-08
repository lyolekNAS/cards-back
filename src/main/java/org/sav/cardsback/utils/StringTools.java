package org.sav.cardsback.utils;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class StringTools {
	public static String normalize(String s) {
		return Normalizer.normalize(s, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.toLowerCase(Locale.ROOT);
	}

	/**
	 * Parses text into separate words, ignoring punctuation marks and not splitting
	 * words by hyphens.
	 *
	 * @param text the text to parse
	 * @return a list of words with punctuation removed but hyphens preserved within words
	 */
	public static List<String> parseWords(String text) {
		if (text == null || text.trim().isEmpty()) {
			return List.of();
		}

		// Remove punctuation except hyphens and apostrophes (which are part of words)
		// Keep hyphens to maintain hyphenated words intact
		String cleanedText = text.replaceAll("[^\\p{L}\\p{N}\\s\\-']", "");

		// Split by whitespace and filter out empty strings
		return Arrays.stream(cleanedText.split("\\s+"))
				.distinct()
				.filter(word -> !word.isEmpty())
				.collect(Collectors.toList());
	}

	/**
	 * Parses text into separate words and returns them as an array.
	 *
	 * @param text the text to parse
	 * @return an array of words with punctuation removed but hyphens preserved
	 */
	public static String[] parseWordsToArray(String text) {
		return parseWords(text).toArray(new String[0]);
	}
}
