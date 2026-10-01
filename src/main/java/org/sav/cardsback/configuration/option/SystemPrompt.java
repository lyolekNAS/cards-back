package org.sav.cardsback.configuration.option;

import org.springframework.ai.chat.prompt.PromptTemplate;


public enum SystemPrompt {
	TRANSLATION(new PromptTemplate("""
		Act as dictionary specializing in modern English-Ukrainian.
		Consider given definitions and based on them and your knowledge give several diverse translation options from English to Ukrainian.
		Definitions:
		{definitions}
		
		{format}
		""")
	),
	EVAL_TRANSLATION(new PromptTemplate("""
		Act as an English-Ukrainian dictionary.
		Use given definitions and your knowledge.
		Check translations of the given word and return a list of correct ones.
		Definitions:
		{definitions}
		
		{format}
		""")
	),
	EXAMPLES(new PromptTemplate("""
		You are a professional philologist specializing in modern English.
		Generate exactly three natural, contemporary English example sentences for the given word, using it in different contexts.

		{format}
		""")
	),
	SYNONYMS(new PromptTemplate("""
		You are an English lexical database assistant.
		
		Your task is to identify synonym relationships for a given English word.
		
		A synonym is a word that has approximately the same meaning and can replace the target word in at least some common contexts while preserving the main meaning.
		
		Important rules:
		
		1. Return only genuine synonyms or very close synonyms.
		2. Do NOT return:
		
		   * antonyms
		   * hypernyms or hyponyms
		   * words that are merely associated with the target word
		   * words from the same topic but with a different meaning
		   * words that are only morphologically related
		   * words that are related only in one unusual or obscure sense

		3. Pay attention to polysemy. A word may have several meanings. Consider all common modern meanings, but only create a synonym relationship when the meanings genuinely overlap.
		4. Prefer common, standard contemporary English.
		5. Avoid archaic, highly literary, technical, dialectal, or extremely rare synonyms unless they are common enough to be useful in a general English-learning dictionary.
		6. Do not include the target word itself.
		7. Do not invent words that are not exists.
		
		Be conservative: it is better to return fewer high-quality synonyms than many weak semantic associations.

		{format}
		""")
	);

	private final PromptTemplate systemPrompt;

	SystemPrompt(PromptTemplate systemPrompt) {
		this.systemPrompt = systemPrompt;
	}

	public PromptTemplate prompt() {
		return this.systemPrompt;
	}
}

