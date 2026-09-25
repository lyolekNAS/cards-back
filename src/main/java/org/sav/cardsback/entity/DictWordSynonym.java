package org.sav.cardsback.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
	name = "dict_word_synonym",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"wordId", "synonymWordId"})
	},
	indexes = {
		@Index(name = "idx_dict_word_synonym_word_id", columnList = "wordId"),
		@Index(name = "idx_dict_word_synonym_synonym_word_id", columnList = "synonymWordId")
	}
)
public class DictWordSynonym {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "wordId", nullable = false)
	@JsonIgnore
	private DictWord lemma;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "synonymWordId", nullable = false)
	@JsonIgnore
	private DictWord synonym;
}

