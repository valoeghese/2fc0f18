package tk.valoeghese.fc0.world.gen.generator;

import tk.valoeghese.fc0.world.gen.GenWorld;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.Random;

public class ScatteredOreGenerator extends Generator<HeightCountGeneratorSettings> {
	protected ScatteredOreGenerator() {
		super("scattered_ore");
	}

	@Override
	public void generate(GenWorld world, HeightCountGeneratorSettings generatorSettings, int startX, int startZ, Random rand) {
		int count = generatorSettings.getCount(rand, startX, startZ);
		int extraAttempts = 3;

		while (count --> 0) {
			int x = startX + rand.nextInt(16);
			int z = startZ + rand.nextInt(16);
			int y = generatorSettings.getY(rand);

			if (world.readTile(x, y, z) == Tile.STONE.id) {
				world.wgWriteTile(x, y, z, Tile.GALENA.id);
			} else {
				if (extraAttempts > 0) {
					extraAttempts--;
					count++;
				}
			}
		}
	}

	public static final HeightCountGeneratorSettings GALENA = new HeightCountGeneratorSettings(152, 1, 60);
}
