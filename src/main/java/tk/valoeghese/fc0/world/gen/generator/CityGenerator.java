package tk.valoeghese.fc0.world.gen.generator;

import tk.valoeghese.fc0.util.maths.MathsUtils;
import tk.valoeghese.fc0.world.gen.GenWorld;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.Random;

public class CityGenerator extends Generator<NoneGeneratorSettings> {
	protected CityGenerator() {
		super("city");
	}

	@Override
	public void generate(GenWorld world, NoneGeneratorSettings generatorSettings, int startX, int startZ, Random rand) {
		// Generate Walls
		for (int xo = 0; xo < 16; ++xo) {
			int x = startX + xo;

			for (int zo = 0; zo < 16; ++zo) {
				int z = startZ + zo;
				int dist = MathsUtils.manhattan(0,0, x, z);

				if (dist >= 50 && dist <= 53) {
					final int height = (dist == 50 || dist == 53) ? 7 : 6;
					int startY = getHeightForGeneration(world, x, z);

					for (int yo = 0; yo < height; ++yo) {
						int y = startY + yo;
						world.wgWriteTile(x, y, z, Tile.STONE_BRICKS.id);
					}
				}
			}
		}
	}
}
