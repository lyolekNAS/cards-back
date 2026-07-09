package org.sav.cardsback.utils;

import java.text.Normalizer;
import java.util.Locale;

public class StringTools {
	public static String normalize(String s) {
		return Normalizer.normalize(s, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.toLowerCase(Locale.ROOT);
	}
}
