package tk.valoeghese.fc0.client.sound;

import tk.valoeghese.fc0.client.Client2fc;
import valoeghese.scalpel.audio.AudioBuffer;
import valoeghese.scalpel.util.ALUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public abstract class SoundEffect {
	public SoundEffect(String name) {
		this.translationKey = "soundfx." + name;
	}

	protected final String translationKey;

	public abstract AudioBuffer getBuffer(Random rand);
	public abstract void genBuffers();

	public final String getTranslationKey() {
		return this.translationKey;
	}

	protected AudioBuffer createBuffer(String s) {
		try {
			return ALUtils.createBuffer(s);
		} catch (IOException e) {
			throw new UncheckedIOException("Exception Loading Sound Effect " + Client2fc.getInstance().language.translate(this.translationKey), e);
		}
	}

	public static Iterable<SoundEffect> getSoundEffects() {
		return SOUND_EFFECTS;
	}

	/**
	 * @return a new Sound Effect with the given parameters. If there are multiple resources, it will pick between them.
	 */
	public static SoundEffect create(String name, String... resources) {
		if (resources.length == 1) return new SingleSoundEffect(name, resources[0]);
		else return new RandomSoundEffect(name, resources);
	}

	private static final Set<SoundEffect> SOUND_EFFECTS = new HashSet<>();

	public static final SoundEffect BUTTON_CLICK = create("button", "button1");
	public static final SoundEffect BUTTON_OK = create("button", "button2");
	public static final SoundEffect CLACK = create("clack", "clack");

	public static final SoundEffect WOOD_PLACE = create("wood_place", "holz3", "holz2");
	public static final SoundEffect WOOD_STEP = create("wood_step", "step1");
	public static final SoundEffect WOOD_BREAK = create("wood_break", "holz1, holz2");

	public static final SoundEffect PLING = create("pling", "pling");

	public static final SoundEffect STONE_PLACE = create("stone_place", "step2");
	public static final SoundEffect STONE_STEP = create("stone_step", "step2");
	public static final SoundEffect STONE_BREAK = create("stone_break", "step1");

	private static final class SingleSoundEffect extends SoundEffect {
		SingleSoundEffect(String name, String resource) {
			super(name);
			this.resourceLocation = "assets/sound/" + resource + ".ogg";
		}

		private final String resourceLocation;
		private AudioBuffer buffer;

		@Override
		public AudioBuffer getBuffer(Random rand) {
			return this.buffer;
		}

		@Override
		public void genBuffers() {
			this.buffer = this.createBuffer(this.resourceLocation);
		}
	}

	private static final class RandomSoundEffect extends SoundEffect {
		RandomSoundEffect(String name, String... resources) {
			super(name);
			this.resourceLocations = Arrays.stream(resources)
					.map(sound -> "assets/sound/" + sound + ".ogg")
					.toArray(String[]::new);
		}

		private final String[] resourceLocations;
		private AudioBuffer[] buffers;

		@Override
		public AudioBuffer getBuffer(Random rand) {
			return this.buffers[rand.nextInt(this.buffers.length)];
		}

		@Override
		public void genBuffers() {
			this.buffers = Arrays.stream(this.resourceLocations)
					.map(this::createBuffer)
					.toArray(AudioBuffer[]::new);
		}
	}
}
