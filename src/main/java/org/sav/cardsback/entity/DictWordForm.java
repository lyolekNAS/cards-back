package org.sav.cardsback.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class DictWordForm {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String wordText;

	@Column(nullable = false)
	private Long freq = 0L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "wordId", nullable = true)
	@JsonIgnore
	private DictWord lemma;
}
