package org.sav.cardsback.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;
import org.sav.cardsback.domain.dictionary.model.WordStates;
import org.sav.cardsback.dto.WordDto;
import org.sav.cardsback.entity.DictWord;
import org.sav.cardsback.entity.DictTrans;
import org.sav.cardsback.entity.DictWordDefinition;
import org.sav.cardsback.entity.DictWordForm;
import org.sav.cardsback.entity.DictWordExamples;
import org.sav.cardsback.entity.Word;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {WordStateMapper.class})
public interface WordMapper {

	@Mapping(target = "dictWordId", source = "dictWord.id")
	@Mapping(
			target = "dictWordFreqSum",
			expression = "java(sumDictWordFreq(entity.getDictWord()))"
	)
	@Mapping(
			target = "rarity",
			expression = "java(calcRarity(entity.getDictWord()))"
	)
	@Mapping(
			target = "isAITranslated",
			expression = "java(isAiTranslated(entity.getDictWord()))"
	)
	WordDto toDto(Word entity);

	@BeanMapping(ignoreByDefault = true)
	@Mapping(target = "dictWordId", source = "id")
	@Mapping(target = "english", source = "wordText")
	@Mapping(target = "description", expression = "java(joinDefinitions(entity))")
	@Mapping(target = "ukrainian", expression = "java(joinTranslations(entity))")
	@Mapping(
			target = "dictWordFreqSum",
			expression = "java(sumDictWordFreq(entity))"
	)
	@Mapping(
			target = "rarity",
			expression = "java(calcRarity(entity))"
	)
	@Mapping(target = "examples", expression = "java(mapExamples(entity))")
	@Mapping(
			target = "isAITranslated",
			expression = "java(isAiTranslated(entity))"
		)
	WordDto toDto(DictWord entity);

	List<WordDto> toDtoList(List<Word> entities);

	@Mapping(target = "dictWord", source = "dictWordId")
	Word toEntity(WordDto dto);

	List<Word> toEntityList(List<WordDto> dtos);
	default DictWord mapDictWord(long id) {
		if (id == 0) return null; // Dto може передати 0, тому підстрахуємо
		DictWord d = new DictWord();
		d.setId(id);
		return d;
	}

	default long sumDictWordFreq(DictWord dictWord) {
		if (dictWord == null || dictWord.getForms() == null) {
			return 0L;
		}

		return dictWord.getForms()
				.stream()
				.mapToLong(DictWordForm::getFreq)
				.sum();
	}

	default int calcRarity(DictWord dictWord) {
		long f = sumDictWordFreq(dictWord);

		if (f > 27_200_000) return 1;
		if (f > 7_300_000) return 2;
		if (f > 2_400_000)  return 3;
		if (f > 800_000)  return 4;
		return 5;
	}

	default boolean isAiTranslated(DictWord dictWord) {
		return dictWord != null && dictWord.hasState(WordStates.AI_TRANSLATED);
	}

	default String joinDefinitions(DictWord dictWord) {
		if (dictWord == null || dictWord.getDefinitions() == null) {
			return "";
		}

		return dictWord.getDefinitions().stream()
				.map(this::formatDefinition)
				.collect(Collectors.joining("\n"));
	}

	default String joinTranslations(DictWord dictWord) {
		if (dictWord == null || dictWord.getTranslations() == null) {
			return "";
		}

		return dictWord.getTranslations().stream()
				.map(DictTrans::getWordText)
				.collect(Collectors.joining(", "));
	}

	default List<String> mapExamples(DictWord dictWord) {
		if (dictWord == null || dictWord.getExamples() == null) {
			return List.of();
		}

		return dictWord.getExamples().stream()
				.map(DictWordExamples::getExample)
				.toList();
	}

	default String formatDefinition(DictWordDefinition definition) {
		if (definition == null) {
			return "";
		}

		return definition.getPartOfSpeach() + ": " + definition.getDefinitionText();
	}

}
