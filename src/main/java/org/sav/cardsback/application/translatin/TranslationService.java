package org.sav.cardsback.application.translatin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.entity.DictTrans;
import org.sav.cardsback.entity.DictWord;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TranslationService {
	private final AzureTranslator azureTranslator;
	private final MyMemoryTranslator myMemoryTranslator;
	private final GoogleTranslator googleTranslator;
	private final AITranslator aiTranslator;

	public List<DictTrans> getTranslations(DictWord word){
		return getAllTranslations(word).stream()
				.map(s -> s == null ? "" : s.trim().toLowerCase())
				.filter(s -> !s.isBlank())
				.distinct()
				.map(s -> {
					DictTrans dt = new DictTrans();
					dt.setWordText(s);
					dt.setLemma(word);
					return dt;
				})
				.toList();
	}

	private List<String> getAllTranslations(DictWord word){
		List<String> allTrans = new ArrayList<>();
//		allTrans.addAll(azureTranslator.processWord(word));
//		allTrans.addAll(myMemoryTranslator.processWord(word));
		allTrans.addAll(googleTranslator.processWord(word));
		allTrans.addAll(aiTranslator.processWord(word));
		return allTrans;
	}
}
