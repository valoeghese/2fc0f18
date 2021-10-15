package tk.valoeghese.fc0.client.sound;

import valoeghese.scalpel.audio.AudioBuffer;
import valoeghese.scalpel.util.ALUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Set;

public class MusicPiece {
	public MusicPiece(String resource) {
		try {
			this.buffer = ALUtils.createBuffer("assets/sound/" + resource + ".ogg");
		} catch (IOException e) {
			throw new UncheckedIOException("Error creating Music Audio Buffer", e);
		}
	}

	// this is automatically removed in alutils#shutdown
	private final AudioBuffer buffer;

	public static final Set<MusicPiece> PIECES = new HashSet<>();
	public static final MusicPiece MAIN_THEME = new MusicPiece("main_theme");
}
