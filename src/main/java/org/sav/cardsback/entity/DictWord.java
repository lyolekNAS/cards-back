package org.sav.cardsback.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.sav.cardsback.domain.dictionary.model.WordStates;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class DictWord {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String wordText;

	@Column(nullable = false)
	private Integer state = 0;

	@OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL)
	@JsonIgnoreProperties("lemma")
	private List<DictWordForm> forms = new ArrayList<>();

	@OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL)
	@JsonIgnoreProperties("lemma")
	private List<DictWordDefinition> definitions = new ArrayList<>();

	@OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL)
	@JsonIgnoreProperties("lemma")
	private List<DictTrans> translations = new ArrayList<>();

	public void addState(WordStates s){
		state = state | s.getId();
	}

	public boolean hasState(WordStates s){
		return (state & s.getId()) > 0;
	}
	public boolean hasNoState(WordStates s){
		return !hasState(s);
	}
}
