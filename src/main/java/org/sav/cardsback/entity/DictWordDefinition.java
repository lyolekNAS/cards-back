package org.sav.cardsback.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class DictWordDefinition {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String partOfSpeach;

	@Column(length = 1500)
	private String definitionText;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "wordId", nullable = true)
	@JsonIgnore
	private DictWord lemma;
}
