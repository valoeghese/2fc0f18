package tk.valoeghese.fc0.world.gen;

import tk.valoeghese.fc0.util.noise.Noise;
import tk.valoeghese.fc0.util.noise.RidgedNoise;
import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.ChunkAccess;
import tk.valoeghese.fc0.world.World;
import tk.valoeghese.fc0.world.gen.ecozone.EcoZone;
import tk.valoeghese.fc0.world.gen.generator.Generator;
import tk.valoeghese.fc0.world.gen.generator.GeneratorSettings;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.Map;
import java.util.Random;

public final class WorldGen {
	public static void updateSeed(long seed) {
		if (noise == null || seed != cachedSeed) {
			noise = new Noise(new Random(seed));
			ridges = new RidgedNoise(new Random(seed + 12));
			sand = new Noise(new Random(seed - 29));
			ecoZone = new Noise(new Random(seed + 31));
		}
	}

	public static <T extends Chunk> T generateChunk(ChunkConstructor<T> constructor, ChunkAccess parent, int chunkX, int chunkZ, long seed, Random rand) {
		byte[] tiles = new byte[16 * 16 * World.WORLD_HEIGHT];
		byte[] meta = new byte[tiles.length];

		int blockX = chunkX << 4;
		int blockZ = chunkZ << 4;

		for (int x = 0; x < 16; ++x) {
			int totalX = x + blockX;

			for (int z = 0; z < 16; ++z) {
				int totalZ = z + blockZ;
				EcoZone zone = getEcoZoneByPosition(totalX, totalZ);

				// ridges
				int height = (int) (10.0 * ridges.sample(totalX / 75.0, totalZ / 75.0)) ;

				double scaleFactor = (1 + noise.sample((totalX - 18) / 290.0, totalZ / 310.0)) / 1.5;

				// main shape
				double mainNoise = noise.sample(totalX / 140.0, totalZ / 140.0);
				height += (int) ((mainNoise > 0) ? 23.0 * scaleFactor * mainNoise : 10.0 * scaleFactor * mainNoise);
				height += 50;

				int sandHeight = (int) (2.1 * sand.sample(totalX / 21.0, totalZ / 21.0));

				if (height >= World.WORLD_HEIGHT) {
					height = World.WORLD_HEIGHT - 1;
				}

				int depth = zone.surface == Tile.SAND.id ? 2 : 1;

				for (int y = 0; y < height; ++y) {
					byte toSet = y > height - depth - 1 ? zone.surface : Tile.STONE.id;

					if (toSet == zone.surface && height < 52) {
						toSet = zone.beach;
					}

					int index = Chunk.index(x, y, z);
					tiles[index] = toSet;

					if (toSet == Tile.GRASS.id && zone.isCold()) {
						meta[index] = 1;
					}
				}

				if (height < 52) {
					for (int y = height; y < 52; ++y) {
						if (y == 51 && zone.isCold()) {
							tiles[Chunk.index(x, y, z)] = Tile.ICE.id;
						} else {
							tiles[Chunk.index(x, y, z)] = Tile.WATER.id;
						}
					}
				}

				// add beaches
				if (height <= 52 + sandHeight) {
					for (int y = 51; y < height; ++y) {
						tiles[Chunk.index(x, y, z)] = zone.beach;
					}
				}
			}
		}

		return constructor.create(parent, chunkX, chunkZ, tiles, meta);
	}

	public static void populateChunk(GenWorld world, Chunk chunk, Random rand) {
		EcoZone zone = getEcoZoneByPosition(chunk.startX, chunk.startZ);

		for (Map.Entry<Generator, GeneratorSettings> generator : zone.getGenerators()) {
			generator.getKey().generate(world, generator.getValue(), chunk.startX, chunk.startZ, rand);
		}
	}

	public static EcoZone getEcoZoneByPosition(double x, double z) {
		return getEcoZone(ecoZone.sample(x * 0.0012, z * 0.0012), ecoZone.sample(x * 0.002 + 4.08, z * 0.002));
	}

	public static EcoZone getEcoZone(double temp, double humidity) {
		if (temp < -0.39) {
			return EcoZone.TUNDRA;
		} else if (temp < -0.27) {
			if (humidity > 0.15) {
				return EcoZone.COLD_WOODLAND;
			} else {
				return EcoZone.TUNDRA;
			}
		} else if (temp < 0.27) {
			if (humidity < -0.15) {
				return EcoZone.TEMPERATE_GRASSLAND;
			} else if (humidity < 0.25) {
				return EcoZone.TEMPERATE_WOODLAND;
			} else {
				return EcoZone.TEMPERATE_RAINFOREST;
			}
		} else {
			if (humidity < -0.2) {
				return EcoZone.DESERT;
			} else if (humidity < 0.2) {
				return EcoZone.TROPICAL_GRASSLAND;
			} else {
				return EcoZone.TROPICAL_RAINFOREST;
			}
		}
	}

	public static double sampleNoise(double x, double y) {
		return noise.sample(x, y);
	}

	private static Noise noise;
	private static Noise ridges;
	private static Noise sand;
	private static Noise ecoZone;
	private static long cachedSeed = 0;

	@FunctionalInterface
	public interface ChunkConstructor<T extends Chunk> {
		T create(ChunkAccess parent, int x, int z, byte[] tiles, byte[] meta);
	}
}
