package org.sav.cardsback.domain.dictionary.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.repository.DictTransRepository;
import org.sav.cardsback.domain.dictionary.repository.DictionaryRepository;
import org.sav.cardsback.dto.LevelBounds;
import org.sav.cardsback.dto.LevelBoundsDto;
import org.sav.cardsback.entity.DictWord;
import org.sav.cardsback.entity.DictWordExamples;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DictionaryService {

	private final DictionaryRepository dictionaryRepository;
	private final DictTransRepository dictTransRepository;
	private final EntityManager entityManager;

	public DictWord save(DictWord dw){
		return dictionaryRepository.save(dw);
	}

	public boolean existsByUserAndDictWord(Long userId, Long wordId){
		return dictionaryRepository.existsByUserAndDictWord(userId, wordId);
	}

	public Optional<DictWord> findWordToProcess(int forbidden, int required){
		return dictionaryRepository.findWordToProcess(forbidden, required);
	}

	public Optional<DictWord> findById(Long id){
		return dictionaryRepository.findById(id);
	}

	public long countWordsToProcess(int forbidden, int required){
		return dictionaryRepository.countWordsToProcess(forbidden, required);
	}

	public Optional<DictWord> findByWordText(String word){
		return dictionaryRepository.findByWordText(word);
	}

	public List<String> getExamples(Long id){
		return findById(id)
				.map(dw -> dw.getExamples().stream().map(DictWordExamples::getExample).toList())
				.orElseGet(List::of);
	}

	public Optional<DictWord> findWordToSuggest(int level, long userId){
		LevelBoundsDto lb = getLevelBounds(level);
		return dictionaryRepository.findWordToSuggest(lb.lowBound(), lb.highBound(), userId);
	}

	public LevelBoundsDto getLevelBounds(int level) {
		int normalizedLevel = Math.clamp(level, 1, 5);
		return new LevelBoundsDto(normalizedLevel, LevelBounds.getBounds(normalizedLevel).getBound(), LevelBounds.getBounds(normalizedLevel - 1).getBound());
	}

	@Transactional
	public void resetWord(Long wordId){
		// Delete definitions linked to this lemma
		entityManager.createQuery("delete from DictWordDefinition d where d.lemma.id = :id")
				.setParameter("id", wordId)
				.executeUpdate();

		// Delete examples linked to this lemma
		entityManager.createQuery("delete from DictWordExamples e where e.lemma.id = :id")
				.setParameter("id", wordId)
				.executeUpdate();

		// Delete translations (uses repository which has @Modifying query)
		dictTransRepository.deleteByLemmaId(wordId);

		// Detach forms: set lemma to null
		entityManager.createQuery("update DictWordForm f set f.lemma = null where f.lemma.id = :id")
				.setParameter("id", wordId)
				.executeUpdate();

		// Reset state of the word to 0
		findById(wordId).ifPresent(dw -> {
			dw.setState(0);
			save(dw);
		});
	}
}
