package tk.valoeghese.fc0.client.sound;

import valoeghese.scalpel.audio.AudioBuffer;
import valoeghese.scalpel.util.ALUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Set;

public class MusicPiece {
	public MusicPiece(String resource) {
		this.resource = "assets/sound/music" + resource + ".ogg";

		if (MusicSystem.shouldGenerateBuffers()) {
			this.generateBuffers();
		}

		PIECES.add(this);
	}

	private final String resource;
	// this is automatically removed in alutils#shutdown
	private AudioBuffer buffer;

	public AudioBuffer getBuffer() {
		return this.buffer;
	}

	void generateBuffers() {
		try {
			this.buffer = ALUtils.createBuffer(this.resource);
		} catch (IOException e) {
			throw new UncheckedIOException("Error creating Music Audio Buffer", e);
		}
	}

	public static final Set<MusicPiece> PIECES = new HashSet<>();
	public static final MusicPiece MAIN_THEME = new MusicPiece("main_theme");
	public static final MusicPiece TOWN_CLAV = new MusicPiece("town");
	public static final MusicPiece TOWN_HARPSICHORD = new MusicPiece("town2");
	public static final MusicPiece FOREST_RILL = new MusicPiece("forest_rill");
}
