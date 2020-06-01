package tk.valoeghese.fc0.world.gen.generator;

import java.util.Random;

public class HeightCountGeneratorSettings implements GeneratorSettings {
	public HeightCountGeneratorSettings(int count, int minY, int maxY) {
		this.count = count;
		this.minY = minY;
		this.dY = maxY - minY + 1;
	}

	private final int count;
	private final int minY;
	private final int dY;

	@Override
	public int getCount(Random rand, int startX, int startZ) {
		return this.count;
	}

	public int getY(Random rand) {
		return rand.nextInt(this.dY) + this.minY;
	}
}
