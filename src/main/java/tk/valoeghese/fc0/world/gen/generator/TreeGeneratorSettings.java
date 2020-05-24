package tk.valoeghese.fc0.world.gen.generator;

import tk.valoeghese.fc0.world.gen.WorldGen;

import java.util.Random;

public class TreeGeneratorSettings implements GeneratorSettings {
	public TreeGeneratorSettings(int baseTreeCount, float treeCountVariation) {
		this(baseTreeCount, treeCountVariation, 3, 3);
	}

	public TreeGeneratorSettings(int baseTreeCount, float treeCountVariation, int baseTreeHeight, int potentialHeightIncrease) {
		this.baseTreeCount = baseTreeCount;
		this.treeCountVariation = treeCountVariation;
		this.baseTreeHeight = baseTreeHeight;
		this.potentialHeightIncrease = potentialHeightIncrease;
	}

	private final int baseTreeCount;
	private final float treeCountVariation;
	private final int baseTreeHeight;
	private final int potentialHeightIncrease;

	@Override
	public int getCount(Random rand, int startX, int startZ) {
		return this.baseTreeCount + (int) (this.treeCountVariation * WorldGen.sampleNoise(startX / 64.0, startZ / 64.0));
	}

	public int getHeight(Random rand) {
		return this.baseTreeHeight + rand.nextInt(this.potentialHeightIncrease);
	}
}
