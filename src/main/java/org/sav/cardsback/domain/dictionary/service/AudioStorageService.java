package org.sav.cardsback.domain.dictionary.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
@Slf4j
public class AudioStorageService {


	@Value("${app-props.path.speech}")
	private String speechPath;

	public Path saveAudio (byte[] audioData, String word) {
		try {
			Path directory = Paths.get(speechPath);

			if (!Files.exists(directory)) {
				Files.createDirectories(directory);
			}

			Path filePath = directory.resolve(word.toLowerCase() + ".mp3");

			Files.write(filePath, audioData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

			log.debug("Saved audio for word '{}' to {}", word, filePath.toAbsolutePath());
			return filePath;

		} catch (IOException e) {
			log.error("Error saving audio for word: {}",  word, e);
			throw new RuntimeException("Could not save audio file", e);
		}
	}
}
