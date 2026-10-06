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
		@UniqueConstraint(columnNames = {"wordId", "synonymText"})
	},
	indexes = {
		@Index(name = "idx_dict_word_synonym_word_id", columnList = "wordId")
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

	@Column(name = "synonymText", nullable = false)
	private String synonym;
}

