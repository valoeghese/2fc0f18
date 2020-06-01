package tk.valoeghese.fc0.world.gen.generator;

import java.util.Random;

public class RandomCountGeneratorSettings implements GeneratorSettings {
	public RandomCountGeneratorSettings(int min, int max) {
		this.min = min;
		this.dCount = max - min + 1;
	}

	private final int min;
	private final int dCount;

	@Override
	public int getCount(Random rand, int startX, int startZ) {
		return this.min + rand.nextInt(this.dCount);
	}
}
