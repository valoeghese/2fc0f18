package tk.valoeghese.fc0.world.gen.generator;

import java.util.Random;

public interface GeneratorSettings {
	int getCount(Random rand, int startX, int startZ);
}
