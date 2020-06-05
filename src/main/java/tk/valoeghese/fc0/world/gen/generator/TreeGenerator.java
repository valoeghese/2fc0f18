package tk.valoeghese.fc0.world.gen.generator;

import tk.valoeghese.fc0.world.gen.GenWorld;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.Random;

public class TreeGenerator extends Generator<TreeGeneratorSettings> {
	TreeGenerator() {
		super("tree");
	}

	@Override
	public void generate(GenWorld world, TreeGeneratorSettings settings, int startX, int startZ, Random rand) {
		int count = settings.getCount(rand, startX, startZ);

		if (rand.nextInt(8) == 0) {
			++count;

			if (rand.nextInt(4) == 0) {
				++count;
			}
		}

		if (count <= 0) {
			return;
		}

		for (int i = 0; i < count; ++i) {
			int x = startX + rand.nextInt(16);
			int z = startZ + rand.nextInt(16);
			int y = getHeightForGeneration(world, x, z);
			int height = settings.getHeight(rand);

			if (world.readTile(x, y - 1, z) != Tile.GRASS.id) {
				continue;
			}

			for (int xo = -2; xo <= 2; ++xo) {
				int totalX = x + xo;

				for (int zo = -2; zo <= 2; ++zo) {
					int totalZ = z + zo;

					for (int yo = 0; yo < 2; ++yo) {
						if (yo == 1 && Math.abs(zo) == 2 && Math.abs(xo) == 2 && rand.nextBoolean()) {
							break;
						}

						int totalY = y + height + yo;

						if (world.isInWorld(totalX, totalY, totalZ)) {
							writeTile(world, totalX, totalY, totalZ, Tile.LEAVES.id);
						}
					}
				}
			}

			int finalCrossY = y + height + 2;

			for (int xo = -1; xo < 2; ++xo) {
				int totalX = xo + x;

				for (int zo = -1; zo < 2; ++zo) {
					if (zo != 0 && xo != 0) {
						continue;
					}

					int totalZ = zo + z;

					if (world.isInWorld(totalX, finalCrossY, totalZ)) {
						writeTile(world, totalX, finalCrossY, totalZ, Tile.LEAVES.id);
					}
				}
			}

			for (int yo = 0; yo < height; ++yo) {
				int totalY = yo + y;

				if (world.isInWorld(x, totalY, z)) {
					world.wgWriteTile(x, totalY, z, Tile.LOG.id);
				}
			}
		}
	}
}
