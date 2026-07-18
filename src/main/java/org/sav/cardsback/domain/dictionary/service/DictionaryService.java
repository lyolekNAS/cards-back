package org.sav.cardsback.domain.dictionary.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.repository.DictionaryRepository;
import org.sav.cardsback.dto.LevelBounds;
import org.sav.cardsback.dto.LevelBoundsDto;
import org.sav.cardsback.entity.DictWord;
import org.sav.cardsback.entity.DictWordExamples;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DictionaryService {

	private final DictionaryRepository dictionaryRepository;

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
}
