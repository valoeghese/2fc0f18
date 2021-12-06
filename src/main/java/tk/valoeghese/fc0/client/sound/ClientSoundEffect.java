package tk.valoeghese.fc0.client.sound;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.world.sound.SoundEffect;
import valoeghese.scalpel.audio.AudioBuffer;
import valoeghese.scalpel.util.ALUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public abstract class ClientSoundEffect extends SoundEffect {
	public ClientSoundEffect(String name) {
		super(name);
	}

	public abstract AudioBuffer getBuffer(Random rand);

	protected AudioBuffer createBuffer(String s) {
		try {
			return ALUtils.createBuffer(s);
		} catch (NullPointerException | IOException e) {
			throw new RuntimeException("Exception Loading Sound Effect " + Client2fc.getInstance().language.translate(this.translationKey), e);
		}
	}

	/**
	 * @return a new Sound Effect with the given parameters. If there are multiple resources, it will pick between them.
	 */
	public static SoundEffect create(String name, String... resources) {
		if (resources.length == 1) return new SingleSoundEffect(name, resources[0]);
		else return new RandomSoundEffect(name, resources);
	}

	private static final class SingleSoundEffect extends ClientSoundEffect {
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
		public void initialise() {
			this.buffer = this.createBuffer(this.resourceLocation);
		}
	}

	private static final class RandomSoundEffect extends ClientSoundEffect {
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
		public void initialise() {
			this.buffers = Arrays.stream(this.resourceLocations)
					.map(this::createBuffer)
					.toArray(AudioBuffer[]::new);
		}
	}
}
