package org.sav.cards.entity;


import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Tag("word")
class WordTest {
	@Test
	void testWord() {
		Word word = new Word();
		word.setUkrainian("ukrainian");
		word.setEnglish("english");
		word.setDescription("description");
		assertEquals("ukrainian", word.getUkrainian());
		assertEquals("english", word.getEnglish());
		assertEquals("description", word.getDescription());
	}
}
