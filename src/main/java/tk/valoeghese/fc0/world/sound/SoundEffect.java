package tk.valoeghese.fc0.world.sound;

import tk.valoeghese.fc0.Game2fc;

import java.util.HashSet;
import java.util.Set;

public class SoundEffect {
	public SoundEffect(String name) {
		this.translationKey = "soundfx." + name;
		SOUND_EFFECTS.add(this);
	}

	protected final String translationKey;

	/**
	 * Sets up the sound effect. Is only required to be run on the client.
	 */
	public void initialise() {
	};

	public final String getTranslationKey() {
		return this.translationKey;
	}

	public static Iterable<SoundEffect> getSoundEffects() {
		return SOUND_EFFECTS;
	}

	private static final Set<SoundEffect> SOUND_EFFECTS = new HashSet<>();

	public static final SoundEffect BUTTON_CLICK = Game2fc.getInstance().createSoundEffect("button", "button1");
	public static final SoundEffect BUTTON_OK = Game2fc.getInstance().createSoundEffect("button", "button2");
	public static final SoundEffect CLACK = Game2fc.getInstance().createSoundEffect("clack", "clack");

	public static final SoundEffect WOOD_PLACE = Game2fc.getInstance().createSoundEffect("wood_place", "holz3", "holz2");
	public static final SoundEffect WOOD_STEP = Game2fc.getInstance().createSoundEffect("wood_step", "step1");
	public static final SoundEffect WOOD_BREAK = Game2fc.getInstance().createSoundEffect("wood_break", "holz1", "holz2");

	public static final SoundEffect PLING = Game2fc.getInstance().createSoundEffect("pling", "pling");

	public static final SoundEffect STONE_PLACE = Game2fc.getInstance().createSoundEffect("stone_place", "stone1");
	public static final SoundEffect STONE_STEP = Game2fc.getInstance().createSoundEffect("stone_step", "step2");
	public static final SoundEffect STONE_BREAK = Game2fc.getInstance().createSoundEffect("stone_break", "stone2", "stone3");
}
