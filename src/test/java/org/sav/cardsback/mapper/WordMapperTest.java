package org.sav.cardsback.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.sav.cardsback.domain.dictionary.model.WordStates;
import org.sav.cardsback.dto.WordDto;
import org.sav.cardsback.dto.WordStateDto;
import org.sav.cardsback.entity.DictWord;
import org.sav.cardsback.entity.DictTrans;
import org.sav.cardsback.entity.DictWordDefinition;
import org.sav.cardsback.entity.DictWordExamples;
import org.sav.cardsback.entity.DictWordForm;
import org.sav.cardsback.entity.Word;
import org.sav.cardsback.entity.WordState;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordMapperTest {

    private WordMapper mapper;

    @BeforeEach
    void setUp() throws Exception {
        mapper = createMapper();
    }

    @Test
    void toDto_mapsEntityToDto() {
        DictWord dictWord = new DictWord();
        dictWord.addState(WordStates.AI_TRANSLATED);

        Word word = new Word();
        word.setId(1L);
        word.setEnglish("hello");
        word.setUkrainian("привіт");
        word.setUserId(42L);
        word.setState(new WordState(WordStateDto.STAGE_1.getId()));
        word.setDictWord(dictWord);

        WordDto dto = mapper.toDto(word);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("hello", dto.getEnglish());
        assertEquals("привіт", dto.getUkrainian());
        assertEquals(WordStateDto.STAGE_1, dto.getState());
        assertTrue(dto.isAITranslated());
    }

    @Test
    void toEntity_mapsDtoToEntity() {
        WordDto dto = new WordDto();
        dto.setId(2L);
        dto.setEnglish("world");
        dto.setUkrainian("світ");
        dto.setState(WordStateDto.STAGE_1);

        Word word = mapper.toEntity(dto);

        assertNotNull(word);
        assertEquals(2L, word.getId());
        assertEquals("world", word.getEnglish());
        assertEquals("світ", word.getUkrainian());
        assertNotNull(word.getState());
        assertEquals(WordStateDto.STAGE_1.getId(), word.getState().getId());
    }

    @Test
    void toDtoList_mapsList() {
        DictWord dictWord1 = new DictWord();
        dictWord1.addState(WordStates.AI_TRANSLATED);

        DictWord dictWord2 = new DictWord();

        Word word1 = new Word();
        Word word2 = new Word();
        word1.setEnglish("hi");
        word1.setDictWord(dictWord1);
        word2.setEnglish("bye");
        word2.setDictWord(dictWord2);

        List<WordDto> dtos = mapper.toDtoList(List.of(word1, word2));

        assertEquals(2, dtos.size());
        assertEquals("hi", dtos.get(0).getEnglish());
        assertEquals("bye", dtos.get(1).getEnglish());
        assertTrue(dtos.get(0).isAITranslated());
        assertFalse(dtos.get(1).isAITranslated());
    }

    @Test
    void toDto_fromDictWord_mapsDto() {
        DictWord dictWord = new DictWord();
        dictWord.setId(99L);
        dictWord.setWordText("hello");
        dictWord.addState(WordStates.AI_TRANSLATED);

        DictWordForm form = new DictWordForm();
        form.setFreq(10L);
        form.setLemma(dictWord);
        dictWord.setForms(List.of(form));

        DictWordDefinition definition = new DictWordDefinition();
        definition.setPartOfSpeach("noun");
        definition.setDefinitionText("greeting");
        definition.setLemma(dictWord);
        dictWord.setDefinitions(List.of(definition));

        DictTrans translation = new DictTrans();
        translation.setWordText("привіт");
        translation.setLemma(dictWord);
        dictWord.setTranslations(List.of(translation));

        DictWordExamples example = new DictWordExamples();
        example.setExample("hello there");
        example.setLemma(dictWord);
        dictWord.setExamples(List.of(example));

        WordDto dto = mapper.toDto(dictWord);

        assertNotNull(dto);
        assertEquals(99L, dto.getDictWordId());
        assertEquals("hello", dto.getEnglish());
        assertEquals("noun: greeting", dto.getDescription());
        assertEquals("привіт", dto.getUkrainian());
        assertEquals(10L, dto.getDictWordFreqSum());
        assertEquals(5, dto.getRarity());
        assertEquals(List.of("hello there"), dto.getExamples());
        assertTrue(dto.isAITranslated());
    }

    private WordMapper createMapper() throws Exception {
        WordMapper wordMapper = Mappers.getMapper(WordMapper.class);
        Field field = wordMapper.getClass().getDeclaredField("wordStateMapper");
        field.setAccessible(true);
        field.set(wordMapper, Mappers.getMapper(WordStateMapper.class));
        return wordMapper;
    }
}
