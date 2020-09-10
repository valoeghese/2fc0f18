package tk.valoeghese.fc0.world.gen.generator;

import tk.valoeghese.fc0.util.maths.Vec2i;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.gen.GenWorld;
import tk.valoeghese.fc0.world.kingdom.Kingdom;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.Random;

public class CityGenerator extends Generator<NoneGeneratorSettings> {
	protected CityGenerator(int size) {
		super("city");
		this.size = size;
		this.sizeOuter = size + 5;
	}

	private final int size;
	private final int sizeOuter;

	@Override
	public void generate(GenWorld world, NoneGeneratorSettings generatorSettings, int startX, int startZ, Random rand) {
		int seed = (int) world.getSeed();
		GameplayWorld gw = world.getGameplayWorld();

		// Generate Walls and paths
		for (int xo = 0; xo < 16; ++xo) {
			int x = startX + xo;

			for (int zo = 0; zo < 16; ++zo) {
				int z = startZ + zo;
				Kingdom kingdom = world.getKingdom(x, z);

				Vec2i centre = kingdom.getCityCentre();
				int dist = centre.manhattan(x, z);

				if (dist > this.sizeOuter) {
					int y = getHeightForGeneration(world, x, z) - 1;

					if (y > 51) {
						Vec2i north = gw.kingdomById(kingdom.neighbourKingdomVec(0, 1, seed)).getCityCentre();
						Vec2i east = gw.kingdomById(kingdom.neighbourKingdomVec(1, 0, seed)).getCityCentre();
						Vec2i south = gw.kingdomById(kingdom.neighbourKingdomVec(0, -1, seed)).getCityCentre();
						Vec2i west = gw.kingdomById(kingdom.neighbourKingdomVec(-1, 0, seed)).getCityCentre();

						// write path
						if (isNear(centre, north, x, z) || isNear(centre, east, x, z)
								|| isNear(centre, south, x, z) || isNear(centre, west, x, z)) {
							world.wgWriteTile(x, y, z, Tile.AIR.id);
							world.wgWriteTile(x, y - 1, z, Tile.GRASS.id);
							world.writeMeta(x, y - 1, z, (byte) 2);
						}
					}
				} else if (dist >= this.size) {
					final int height = (dist == this.size || dist == this.sizeOuter) ? 9 : 8;
					int startY = getHeightForGeneration(world, x, z);

					if (startY > 51) {
						Vec2i north = gw.kingdomById(kingdom.neighbourKingdomVec(0, 1, seed)).getCityCentre();
						Vec2i east = gw.kingdomById(kingdom.neighbourKingdomVec(1, 0, seed)).getCityCentre();
						Vec2i south = gw.kingdomById(kingdom.neighbourKingdomVec(0, -1, seed)).getCityCentre();
						Vec2i west = gw.kingdomById(kingdom.neighbourKingdomVec(-1, 0, seed)).getCityCentre();

						// write gates
						if (isNear(centre, north, x, z) || isNear(centre, east, x, z)
								|| isNear(centre, south, x, z) || isNear(centre, west, x, z)) {
							for (int yo = 4; yo < height; ++yo) {
								int y = startY + yo;
								world.wgWriteTile(x, y, z, Tile.STONE_BRICKS.id);
							}
						} else { // write wall
							for (int yo = 0; yo < height; ++yo) {
								int y = startY + yo;
								world.wgWriteTile(x, y, z, Tile.STONE_BRICKS.id);
							}
						}
					}
				}
			}
		}
	}

	private static boolean isNear(Vec2i locA, Vec2i locB, int x, int y) {
		float m = (float) (locB.getY() - locA.getY()) / (float) (locB.getX() - locA.getX());
		float targetY = m * x + locA.getY() - m * locA.getX();
		return Math.abs(y - targetY) < 5;
	}
}
