package tk.valoeghese.fc0.world.gen.generator;

import tk.valoeghese.fc0.world.World;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.Random;

public class GroundFoliageGenerator extends Generator<GroundFoliageGeneratorSettings> {
	GroundFoliageGenerator() {
		super("ground_foliage");
	}

	@Override
	public void generate(World world, GroundFoliageGeneratorSettings settings, int startX, int startZ, Random rand) {
		int count = settings.getCount(rand, startX, startZ);

		while (count --> 0) {
			int x = startX + rand.nextInt(16);
			int z = startZ + rand.nextInt( 16);
			int y = getHeightForGeneration(world, x, z);
			Tile tile = settings.pickTile(rand);

			if (world.isInWorld(x, y, z)) {
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
