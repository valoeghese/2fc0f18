package tk.valoeghese.fc0.world.gen.generator;

import tk.valoeghese.fc0.world.gen.GenWorld;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.Random;

public class GroundFoliageGenerator extends Generator<GroundFoliageGeneratorSettings> {
	GroundFoliageGenerator() {
		super("ground_foliage");
	}

	@Override
	public void generate(GenWorld world, GroundFoliageGeneratorSettings settings, int startX, int startZ, Random rand) {
		int count = settings.getCount(world, rand, startX, startZ);

		while (count --> 0) {
			int x = startX + rand.nextInt(16);
			int z = startZ + rand.nextInt( 16);
			int y = getHeightForGeneration(world, x, z);
			Tile tile = settings.pickTile(rand);

			if (y > 51 && world.isInWorld(x, y, z)) {
				if (world.readTile(x, y, z) == Tile.WATER.id || (world.readTile(x, y - 1, z) == Tile.GRASS.id && world.readMeta(x, y - 1, z) == 2)) {
					continue;
				}

				world.writeTile(x, y, z, tile.id);

				if (tile == Tile.CACTUS) {
					++y;

					if (world.isInWorld(x, y, z)) {
						world.writeTile(x, y, z, tile.id);
					}
				}
			}
		}
	}
}
