package org.sav.cardsback.repository;

import org.junit.jupiter.api.Test;
import org.sav.cardsback.domain.dictionary.repository.DictionaryRepository;
import org.sav.cardsback.entity.DictWord;
import org.sav.cardsback.entity.UserDictWord;
import org.sav.cardsback.entity.Word;
import org.sav.cardsback.entity.WordState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DictionaryRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private DictionaryRepository dictionaryRepository;

	@Test
	void findByWordText_whenExists_returnsWord() {
		DictWord dw = createAndPersistDictWord("alpha", 0);

		Optional<DictWord> result = dictionaryRepository.findByWordText("alpha");

		assertTrue(result.isPresent());
		assertEquals(dw.getId(), result.get().getId());
	}

	@Test
	void deleteByWordText_whenStateIsZero_deletesWord() {
		createAndPersistDictWord("beta", 0);

		dictionaryRepository.deleteByWordText("beta");
		entityManager.flush();
		entityManager.clear();

		assertTrue(dictionaryRepository.findByWordText("beta").isEmpty());
	}

	@Test
	void deleteByWordText_whenStateNotZero_doesNotDeleteWord() {
		createAndPersistDictWord("gamma", 1);

		dictionaryRepository.deleteByWordText("gamma");
		entityManager.flush();
		entityManager.clear();

		assertTrue(dictionaryRepository.findByWordText("gamma").isPresent());
	}

	@Test
	void existsByUserAndDictWord_whenUserDictWordExists_returnsTrue() {
		DictWord dw = createAndPersistDictWord("delta", 0);
		UserDictWord udw = new UserDictWord();
		udw.setUserId(10L);
		udw.setLemma(dw);
		entityManager.persistAndFlush(udw);

		boolean result = dictionaryRepository.existsByUserAndDictWord(10L, dw.getId());

		assertTrue(result);
	}

	@Test
	void existsByUserAndDictWord_whenWordExistsForUser_returnsTrue() {
		DictWord dw = createAndPersistDictWord("epsilon", 0);
		WordState state = createAndPersistWordState(1);
		Word word = new Word();
		word.setEnglish("epsilon");
		word.setUkrainian("епсилон");
		word.setUserId(20L);
		word.setState(state);
		word.setDictWord(dw);
		word.setEnglishCnt(0);
		word.setUkrainianCnt(0);
		entityManager.persistAndFlush(word);

		boolean result = dictionaryRepository.existsByUserAndDictWord(20L, dw.getId());

		assertTrue(result);
	}

	@Test
	void existsByUserAndDictWord_whenNoLinks_returnsFalse() {
		DictWord dw = createAndPersistDictWord("zeta", 0);

		boolean result = dictionaryRepository.existsByUserAndDictWord(30L, dw.getId());

		assertFalse(result);
	}

	@Test
	void findWordToProcess_returnsWordMatchingBitmasks() {
		int required = 1;
		int forbidden = 2;

		DictWord good = createAndPersistDictWord("good", 1);
		createAndPersistDictWord("bad-forbidden", 3);
		createAndPersistDictWord("bad-required", 0);

		Optional<DictWord> result = dictionaryRepository.findWordToProcess(forbidden, required);

		assertTrue(result.isPresent());
		assertEquals(good.getId(), result.get().getId());
	}

	@Test
	void countWordsToProcess_countsOnlyMatchingWords() {

		int required = 1;
		int forbidden = 2;

		createAndPersistDictWord("match-1", 1);
		createAndPersistDictWord("match-2", 5);
		createAndPersistDictWord("no-required", 0);
		createAndPersistDictWord("has-forbidden", 3);

		long count = dictionaryRepository.countWordsToProcess(forbidden, required);

		assertEquals(2L, count);
	}

	private DictWord createAndPersistDictWord(String wordText, int state) {
		DictWord dw = new DictWord();
		dw.setWordText(wordText);
		dw.setState(state);
		return entityManager.persistAndFlush(dw);
	}

	private WordState createAndPersistWordState(int id) {
		WordState state = new WordState(id);
		state.setName("state-" + id);
		return entityManager.persistAndFlush(state);
	}
}
