package tk.valoeghese.fc0.client.sound;

import valoeghese.scalpel.audio.AudioSource;

import java.util.Random;

public class SoundEffectDispatcher {
	/**
	 * Creates a new manager for playing sound effects.
	 * @param cap the maximum number of possible simultaneous sound effects. The higher the cap, the more audio sources are generated in memory, however!
	 */
	public SoundEffectDispatcher(int cap) {
		this.sources = new AudioSource[cap];

		for (int i = 0; i < cap; ++i)  {
			this.sources[i] = new AudioSource();
		}
	}

	private volatile int currentIndex; // just in case (is this necessary? maybe network thread and game thread will both try play sounds...)
	private final AudioSource[] sources;

	public void playSound(ClientSoundEffect effect, float dx, float dy, float dz, float volume) {
		AudioSource source = this.sources[this.currentIndex++];
		if (this.currentIndex == this.sources.length) this.currentIndex = 0;

		source.setPosition(dx, dy, dz);
		source.setGain(volume);
		source.attachBufferData(effect.getBuffer(RANDOM));
		source.play();
	}

	/**
	 * Destroys all sources in this dispatcher.
	 */
	public void destroy() {
		for (AudioSource source : this.sources) {
			source.destroy();
		}
	}

	@Override
	public String toString() {
		return "SoundEffectDispatcher{" +
				"size=" + this.sources.length +
				", currentIndex=" + this.currentIndex +
				'}';
	}

	private static final Random RANDOM = new Random();
}
