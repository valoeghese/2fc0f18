package tk.valoeghese.fc0.world.tile;

import tk.valoeghese.fc0.world.sound.SoundEffect;

public enum SoundFX {
	STONE(SoundEffect.STONE_BREAK, SoundEffect.STONE_PLACE, SoundEffect.STONE_STEP),
	WOOD(SoundEffect.WOOD_BREAK, SoundEffect.WOOD_PLACE, SoundEffect.WOOD_STEP),
	GRAIN(SoundEffect.GRAIN, SoundEffect.GRAIN, SoundEffect.GRAIN),
	ICE(SoundEffect.PLING, SoundEffect.PLING, SoundEffect.STONE_STEP),
	PLANT(SoundEffect.PLANT, SoundEffect.PLANT, SoundEffect.PLANT_STEP);


	SoundFX(SoundEffect breakSound, SoundEffect placeSound, SoundEffect stepSound) {
		this.breakSound = breakSound;
		this.placeSound = placeSound;
		this.stepSound = stepSound;
	}

	private final SoundEffect breakSound;
	private final SoundEffect placeSound;
	private final SoundEffect stepSound;

	public SoundEffect getBreakSound() {
		return this.breakSound;
	}

	public SoundEffect getPlaceSound() {
		return this.placeSound;
	}

	public SoundEffect getStepSound() {
		return this.stepSound;
	}
}
