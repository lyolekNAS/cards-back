package org.sav.cardsback.application.wordnik;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sav.cardsback.domain.dictionary.model.PartOfSpeech;
import org.sav.cardsback.domain.dictionary.model.WordStates;
import org.sav.cardsback.domain.dictionary.service.WordProcessingService;
import org.sav.cardsback.dto.WordDto;
import org.sav.cardsback.entity.DictWord;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class WordnikRandomWordImporterTest {

	@Mock
	private WordProcessingService wordProcessingService;

	private WordnikRandomWordImporter importer;
	private MockRestServiceServer server;

	@BeforeEach
	void setUp() {
		importer = new WordnikRandomWordImporter(wordProcessingService);
		ReflectionTestUtils.setField(importer, "apiKey", "test-key");

		RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(importer, "restTemplate");
		server = MockRestServiceServer.createServer(restTemplate);
	}

	@Test
	void importRandomWords_filtersMultiWordAndFakeAndUnsuitable() {
		DictWord good = new DictWord();
		good.setWordText("hello");
		good.setState(0);

		DictWord fake = new DictWord();
		fake.setWordText("ghost");
		fake.setState(WordStates.FAKE.getId());

		WordDto dto = WordDto.builder().english("hello").build();

		when(wordProcessingService.processWord("hello")).thenReturn(good);
		when(wordProcessingService.processWord("ghost")).thenReturn(fake);
		when(wordProcessingService.isWordSuitable(1L, good)).thenReturn(true);
		when(wordProcessingService.dtoFromDict(good)).thenReturn(dto);

		server.expect(once(), requestTo(org.hamcrest.Matchers.containsString("api_key=test-key")))
				.andExpect(requestTo(org.hamcrest.Matchers.containsString("includePartOfSpeech=" + PartOfSpeech.getAllDisplayNamesLowercase())))
				.andRespond(withSuccess("""
						[
						  {"word":"HELLO"},
						  {"word":"two words"},
						  {"word":"ghost"}
						]
						""", MediaType.APPLICATION_JSON));

		List<WordDto> result = importer.importRandomWords(1L);

		assertEquals(1, result.size());
		assertEquals("hello", result.getFirst().getEnglish());
		verify(wordProcessingService).processWord("hello");
		verify(wordProcessingService).processWord("ghost");
		verify(wordProcessingService, never()).processWord("two words");
		verify(wordProcessingService, never()).isWordSuitable(any(Long.class), eq(fake));
		server.verify();
	}

	@Test
	void importRandomWords_whenWordNotSuitable_returnsEmptyList() {
		DictWord processed = new DictWord();
		processed.setWordText("alpha");
		processed.setState(0);

		when(wordProcessingService.processWord("alpha")).thenReturn(processed);
		when(wordProcessingService.isWordSuitable(2L, processed)).thenReturn(false);

		server.expect(once(), requestTo(org.hamcrest.Matchers.containsString("api_key=test-key")))
				.andRespond(withSuccess("""
						[
						  {"word":"ALPHA"}
						]
						""", MediaType.APPLICATION_JSON));

		List<WordDto> result = importer.importRandomWords(2L);

		assertTrue(result.isEmpty());
		verify(wordProcessingService, never()).dtoFromDict(any(DictWord.class));
		server.verify();
	}

	@Test
	void importRandomWords_whenResponseMalformed_returnsEmptyList() {
		server.expect(once(), requestTo(org.hamcrest.Matchers.containsString("api_key=test-key")))
				.andRespond(withSuccess("{not-json}", MediaType.APPLICATION_JSON));

		List<WordDto> result = importer.importRandomWords(3L);

		assertTrue(result.isEmpty());
		server.verify();
	}
}
