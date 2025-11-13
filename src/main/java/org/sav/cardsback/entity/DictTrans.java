package org.sav.cardsback.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
	uniqueConstraints = {
			@UniqueConstraint(columnNames = {"wordId", "wordText"})
	}
)
public class DictTrans {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "wordId", nullable = true)
	@JsonIgnore
	private DictWord lemma;

	private String wordText;
}
