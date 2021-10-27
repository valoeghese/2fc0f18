package tk.valoeghese.fc0.world.gen.generator;

import tk.valoeghese.fc0.util.maths.MathsUtils;
import tk.valoeghese.fc0.world.gen.GenWorld;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.Random;

public class TreeGenerator extends Generator<TreeGeneratorSettings> {
	TreeGenerator() {
		super("tree");
	}

	@Override
	public void generate(GenWorld world, TreeGeneratorSettings settings, int startX, int startZ, Random rand) {
		int count = settings.getCount(world, rand, startX, startZ);

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
			int metaBelow = world.readMeta(x, y - 1, z);

			if (y < 52 || world.readTile(x, y - 1, z) != Tile.GRASS.id || metaBelow == 2) {
				continue;
			}

			if (metaBelow == 1 && rand.nextBoolean()) {
				this.generateConiferTreeLeaves(world, rand, x, y, z, height);
			} else {
				this.generateSimpleTreeLeaves(world, rand, x, y, z, height);
			}

			for (int yo = 0; yo < height; ++yo) {
				int totalY = yo + y;

				if (world.isInWorld(x, totalY, z)) {
					world.writeTile(x, totalY, z, Tile.LOG.id);
				}
			}
		}
	}

	private final void generateConiferTreeLeaves(GenWorld world, Random rand, int x, int y, int z, int height) {
		int stage = 0;
		for (int yo = height + 2; yo >= 2; --yo, stage++) {
			int thickness = -2; // this is 1 thickness due to fancy maths

			if ((stage & 1) == 1) {
				thickness = stage / 2; // alternate 1 thickness with 0,1,2 which is treated as 0,2,3 due to the next if and maths
			}

			if (thickness != 0) {
				thickness = Math.abs(thickness + 1); // the magic maths transformer I mentioned above.
				int worldY = y + yo;

				for (int xo = -thickness; xo <= thickness; ++xo) {
					int worldX = x + xo;

					for (int zo = -thickness; zo <= thickness; ++zo) {
						if (MathsUtils.manhattan(0, 0, xo, zo) <= thickness) {
							int worldZ = z + zo;

							if (world.isInWorld(worldX, worldY, worldZ)  && canLeavesReplace(world.readTile(worldX, worldY, worldZ))) {
								writeTile(world, worldX, worldY, worldZ, Tile.LEAVES.id);
							}
						}
					}
				}
			}
		}
	}

	private final void generateSimpleTreeLeaves(GenWorld world, Random rand, int x, int y, int z, int height) {
		for (int xo = -2; xo <= 2; ++xo) {
			int totalX = x + xo;

			for (int zo = -2; zo <= 2; ++zo) {
				int totalZ = z + zo;

				for (int yo = 0; yo < 2; ++yo) {
					if (yo == 1 && Math.abs(zo) == 2 && Math.abs(xo) == 2 && rand.nextBoolean()) {
						break;
					}

					int totalY = y + height + yo;

					if (world.isInWorld(totalX, totalY, totalZ) && canLeavesReplace(world.readTile(totalX, totalY, totalZ))) {
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

				if (world.isInWorld(totalX, finalCrossY, totalZ) && canLeavesReplace(world.readTile(totalX, finalCrossY, totalZ))) {
					writeTile(world, totalX, finalCrossY, totalZ, Tile.LEAVES.id);
				}
			}
		}
	}

	private static boolean canLeavesReplace(byte tileId) {
		return tileId == Tile.GRASS.id || tileId == Tile.STONE.id || tileId == Tile.LEAVES.id || Tile.BY_ID[tileId].isCross();
	}
}
