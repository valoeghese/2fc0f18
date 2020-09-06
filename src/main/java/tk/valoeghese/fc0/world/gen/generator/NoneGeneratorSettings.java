package tk.valoeghese.fc0.world.gen.generator;

import tk.valoeghese.fc0.world.gen.GenWorld;

import java.util.Random;

public class NoneGeneratorSettings implements GeneratorSettings {
	@Override
	public int getCount(GenWorld world, Random rand, int startX, int startZ) {
		return 0;
	}
}
