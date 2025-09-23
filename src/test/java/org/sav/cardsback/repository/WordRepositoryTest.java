package org.sav.cardsback.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.sav.cardsback.entity.Word;
import org.sav.cardsback.entity.WordState;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class WordRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WordRepository wordRepository;

    @Test
    void findAllByUserId_shouldReturnWordsForUser() {
        WordState state = createAndPersistWordState(1);
        createAndPersistWord("test1", "тест1", 1L, state);
        createAndPersistWord("test2", "тест2", 1L, state);
        createAndPersistWord("test3", "тест3", 2L, state);

        List<Word> result = wordRepository.findAllByUserId(1L);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(w -> w.getEnglish().equals("test1")));
        assertTrue(result.stream().anyMatch(w -> w.getEnglish().equals("test2")));
    }

    @Test
    void findByUserIdAndEnglish_shouldReturnWord_whenExists() {
        WordState state = createAndPersistWordState(1);
        createAndPersistWord("test", "тест", 1L, state);

        Word result = wordRepository.findByUserIdAndEnglish(1L, "test");

        assertNotNull(result);
        assertEquals("test", result.getEnglish());
        assertEquals(1L, result.getUserId());
    }

    @Test
    void findByUserIdAndEnglish_shouldReturnNull_whenNotExists() {
        Word result = wordRepository.findByUserIdAndEnglish(1L, "nonexistent");

        assertNull(result);
    }

    @Test
    void findByIdAndUserId_shouldReturnWord_whenExists() {
        WordState state = createAndPersistWordState(1);
        Word word = createAndPersistWord("test", "тест", 1L, state);

        Word result = wordRepository.findByIdAndUserId(word.getId(), 1L);

        assertNotNull(result);
        assertEquals(word.getId(), result.getId());
        assertEquals(1L, result.getUserId());
    }

    @Test
    void findByIdAndUserId_shouldReturnNull_whenWrongUser() {
        WordState state = createAndPersistWordState(1);
        Word word = createAndPersistWord("test", "тест", 1L, state);

        Word result = wordRepository.findByIdAndUserId(word.getId(), 2L);

        assertNull(result);
    }

    @Test
    void findByWordToTrain_shouldReturnWordsWithStates123() {
        WordState state1 = createAndPersistWordState(1);
        WordState state2 = createAndPersistWordState(2);
        WordState state3 = createAndPersistWordState(3);
        WordState state0 = createAndPersistWordState(0);

        createAndPersistWord("test1", "тест1", 1L, state1);
        createAndPersistWord("test2", "тест2", 1L, state2);
        createAndPersistWord("test3", "тест3", 1L, state3);
        createAndPersistWord("test4", "тест4", 1L, state0);

        List<Word> result = wordRepository.findWordToTrain(1L);

        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(w -> 
            w.getState().getId() == 1 || w.getState().getId() == 2 || w.getState().getId() == 3));
    }

    @Test
    void updateEnglishCnt_shouldUpdateCountAndLastTrain() {
        WordState state = createAndPersistWordState(1);
        Word word = createAndPersistWord("test", "тест", 1L, state);
        word.setEnglishCnt(0);
        entityManager.persistAndFlush(word);

        wordRepository.updateEnglishCnt(word.getId(), 5);
        entityManager.clear();

        Word updated = entityManager.find(Word.class, word.getId());
        assertEquals(5, updated.getEnglishCnt());
        assertNotNull(updated.getLastTrain());
    }

    @Test
    void updateUkrainianCnt_shouldUpdateCountAndLastTrain() {
        WordState state = createAndPersistWordState(1);
        Word word = createAndPersistWord("test", "тест", 1L, state);
        word.setUkrainianCnt(0);
        entityManager.persistAndFlush(word);

        wordRepository.updateUkrainianCnt(word.getId(), 3);
        entityManager.clear();

        Word updated = entityManager.find(Word.class, word.getId());
        assertEquals(3, updated.getUkrainianCnt());
        assertNotNull(updated.getLastTrain());
    }

    @Test
    void updateState_shouldUpdateWordState() {
        WordState state1 = createAndPersistWordState(1);
        createAndPersistWordState(2);
        Word word = createAndPersistWord("test", "тест", 1L, state1);

        wordRepository.updateState(word.getId(), 2);
        entityManager.clear();

        Word updated = entityManager.find(Word.class, word.getId());
        assertEquals(2, updated.getState().getId());
    }

    private WordState createAndPersistWordState(Integer id) {
        WordState state = new WordState(id);
        state.setName("State " + id);
        return entityManager.persistAndFlush(state);
    }

    private Word createAndPersistWord(String english, String ukrainian, Long userId, WordState state) {
        Word word = new Word();
        word.setEnglish(english);
        word.setUkrainian(ukrainian);
        word.setUserId(userId);
        word.setState(state);
        word.setEnglishCnt(0);
        word.setUkrainianCnt(0);
        word.setLastTrain(LocalDateTime.now());
        return entityManager.persistAndFlush(word);
    }
}