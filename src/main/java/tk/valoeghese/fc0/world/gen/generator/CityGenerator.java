package tk.valoeghese.fc0.world.gen.generator;

import tk.valoeghese.fc0.util.maths.MathsUtils;
import tk.valoeghese.fc0.util.maths.Vec2i;
import tk.valoeghese.fc0.util.noise.Noise;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.TileAccess;
import tk.valoeghese.fc0.world.gen.GenWorld;
import tk.valoeghese.fc0.world.kingdom.Kingdom;
import tk.valoeghese.fc0.world.kingdom.KingdomIDMapper;
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

		boolean roadsX = 0 == ((startX >> 5) & 0b1);
		boolean roadsZ = 0 == ((startZ >> 6) & 0b1);
		final int houseLimit = this.size - 10;

		for (int xo = 0; xo < 16; ++xo) {
			int x = startX + xo;

			for (int zo = 0; zo < 16; ++zo) {
				int z = startZ + zo;
				// Determine Where in the kingdom we are
				Kingdom kingdom = world.getKingdom(x, z);

				Vec2i centre = kingdom.getCityCentre();
				int dist = centre.manhattan(x, z);

				if (dist > this.sizeOuter) {
					int y = getHeightForGeneration(world, x, z) - 1;

					if (y > 51) {
						if (isOnPath(gw, x, z, kingdom, centre, seed)) {
							world.writeTile(x, y, z, Tile.AIR.id);
							world.writeTile(x, y - 1, z, Tile.GRASS.id);
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
								world.writeTile(x, y, z, Tile.STONE_BRICKS.id);
							}
						} else { // write wall
							for (int yo = 0; yo < height; ++yo) {
								int y = startY + yo;
								world.writeTile(x, y, z, Tile.STONE_BRICKS.id);
							}
						}
					}
				} else {
					int y = getHeightForGeneration(world, x, z) - 1;

					if (y > 51) {
						// Generate Cities
						if (dist < houseLimit && xo == 8 && zo == 8) {
							final int houseHeight = 5;
							final int wallHeight = houseHeight;

							// Generate City House
							// Floor and Walls
							for (int xoo = -5; xoo < 5; ++xoo) {
								boolean xedge = xoo == -5 || xoo == 4;
								int xx = x + xoo;

								for (int zoo = -5; zoo < 5; ++zoo) {
									int zz = z + zoo;
									world.writeTile(xx, y, zz, Tile.PLANKS.id);

									if (xedge || zoo == -5 || zoo == 4) {
										for (int yy = 0; yy < wallHeight; ++yy) {
											world.writeTile(xx, y + yy, zz, Tile.BRICKS.id);
										}
									}
								}
							}

							// Roof
							for (int yy = -1; yy < 2; ++yy) {
								int width = 6 - yy;
								int finalY = y + yy + houseHeight;

								int l = -width;
								int h = width - 1;

								for (int xoo = -width; xoo < width; ++xoo) {
									int finalX = x + xoo;

									for (int zoo = -width; zoo < width; ++zoo) {
										int finalZ = z + zoo;

										if (yy > -1 || zoo == l || zoo == h || xoo == l || xoo == h) {
											if (world.isInWorld(finalX, finalY, finalZ)) {
												world.writeTile(finalX, finalY, finalZ, Tile.STONE_BRICKS.id);
											}
										}
									}
								}
							}

							// Pillars
							for (int yy = 0; yy < houseHeight; ++yy) {
								if (y + yy < TileAccess.WORLD_HEIGHT) {
									world.writeTile(x - 6, y + yy, z - 6, Tile.LOG.id);
									world.writeTile(x + 5, y + yy, z + 5, Tile.LOG.id);
									world.writeTile(x + 5, y + yy, z - 6, Tile.LOG.id);
									world.writeTile(x - 6, y + yy, z + 5, Tile.LOG.id);
								}
							}
						}

						if ((roadsX && xo < 2) || (roadsZ && zo < 3)) {
							// Generate City Roads
							world.writeTile(x, y, z, Tile.AIR.id);
							world.writeTile(x, y - 1, z, Tile.GRASS.id);
							world.writeMeta(x, y - 1, z, (byte) 2);
						}
					}
				}
			}
		}
	}

	private static boolean isNear(Vec2i locA, Vec2i locB, int x, int y) {
		return MathsUtils.distanceLineBetween(locA.getX(), locA.getY(), locB.getX(), locB.getY(), x, y) < 5;
	}

	public static boolean isOnRoad(KingdomIDMapper gw, int x, int z, Kingdom kingdom, Vec2i centre, int seed) {
		// Generate Kingdom Roads
		Vec2i west = gw.kingdomById(kingdom.neighbourKingdomVec(0, 1, seed)).getCityCentre();
		Vec2i south = gw.kingdomById(kingdom.neighbourKingdomVec(1, 0, seed)).getCityCentre();
		Vec2i east = gw.kingdomById(kingdom.neighbourKingdomVec(0, -1, seed)).getCityCentre();
		Vec2i north = gw.kingdomById(kingdom.neighbourKingdomVec(-1, 0, seed)).getCityCentre();

		boolean southside = x > centre.getX();
		boolean westside = z > centre.getY();

		// write road if at a road location
		return westside && isNear(centre, west, x, z) || southside && isNear(centre, south, x, z)
				|| !westside && isNear(centre, east, x, z) || !southside && isNear(centre, north, x, z);
	}

	public static boolean isOnPath(KingdomIDMapper gw, int x, int z, Kingdom kingdom, Vec2i centre, int seed) {
		if (isOnRoad(gw, x, z, kingdom, centre, seed)) {
			return true;
		} else {
			// generate paths at path locations
			double path = PATH_NOISE.sample((double) x / 400.0, (double) z / 400.0);
			return false;// path > 0 && path < 0.019;
		}
	}

	public static boolean isInCity(TileAccess world, int x, int z, int size) {
		Kingdom kingdom = world.getKingdom(x, z);
		Vec2i centre = kingdom.getCityCentre();
		int dist = centre.manhattan(x, z);
		return dist < size + 5;
	}

	private static final Noise PATH_NOISE = new Noise(new Random(69420));
}
