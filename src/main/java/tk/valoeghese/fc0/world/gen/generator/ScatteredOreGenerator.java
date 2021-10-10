package tk.valoeghese.fc0.world.gen.generator;

import tk.valoeghese.fc0.world.World;
import tk.valoeghese.fc0.world.gen.GenWorld;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.Random;

public class ScatteredOreGenerator extends Generator<OreGeneratorSettings> {
	protected ScatteredOreGenerator() {
		super("scattered_ore");
	}

	@Override
	public void generate(GenWorld world, OreGeneratorSettings generatorSettings, int startX, int startZ, Random rand) {
		int count = generatorSettings.getCount(world, rand, startX, startZ);
		int extraAttempts = 3;

		while (count --> 0) {
			int x = startX + rand.nextInt(16);
			int z = startZ + rand.nextInt(16);
			int y = generatorSettings.getY(rand);

			if (world.readTile(x, y, z) == Tile.STONE.id) {
				world.wgWriteTile(x, y, z, generatorSettings.ore);

				if (y < World.WORLD_HEIGHT) {
					if (world.readTile(x, y + 1, z) == Tile.STONE.id) {
						world.wgWriteTile(x, y + 1, z, generatorSettings.ore);
						//TODO write meta?
					}
				}

				if (y > 0) {
					if (world.readTile(x, y - 1, z) == Tile.STONE.id) {
						world.wgWriteTile(x, y - 1, z, generatorSettings.ore);
					}
				}

				if (world.isInWorld(x, y, z + 1)) {
					if (world.readTile(x, y, z + 1) == Tile.STONE.id) {
						world.wgWriteTile(x, y, z + 1, generatorSettings.ore);
					}
				}

				if (world.isInWorld(x, y, z - 1)) {
					if (world.readTile(x, y, z - 1) == Tile.STONE.id) {
						world.wgWriteTile(x, y, z - 1, generatorSettings.ore);
					}
				}

				if (world.isInWorld(x - 1, y, z)) {
					if (world.readTile(x - 1, y, z) == Tile.STONE.id) {
						world.wgWriteTile(x - 1, y, z, generatorSettings.ore);
					}
				}

				if (world.isInWorld(x + 1, y, z)) {
					if (world.readTile(x + 1, y, z) == Tile.STONE.id) {
						world.wgWriteTile(x + 1, y, z, generatorSettings.ore);
					}
				}
			} else {
				if (extraAttempts > 0) {
					extraAttempts--;
					count++;
				}
			}
		}
	}

	public static final OreGeneratorSettings GALENA = new OreGeneratorSettings(25, 1, 60, Tile.GALENA.id);
	public static final OreGeneratorSettings MAGNETITE = new OreGeneratorSettings(15, 11, 70, Tile.MAGNETITE.id);
	public static final OreGeneratorSettings COAL = new OreGeneratorSettings(37, 1, 100, Tile.COAL.id);
	public static final OreGeneratorSettings EXTRA_COAL = new OreGeneratorSettings(6, 50, 100, Tile.COAL.id);
}
