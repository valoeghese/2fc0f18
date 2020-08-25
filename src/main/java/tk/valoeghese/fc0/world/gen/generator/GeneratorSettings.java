package tk.valoeghese.fc0.world.gen.generator;

import tk.valoeghese.fc0.world.gen.GenWorld;

import java.util.Random;

public interface GeneratorSettings {
	int getCount(GenWorld world, Random rand, int startX, int startZ);
}
