package tk.valoeghese.fc0.world.gen;

import tk.valoeghese.fc0.util.Pair;
import tk.valoeghese.fc0.util.noise.Noise;
import tk.valoeghese.fc0.util.noise.RidgedNoise;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.chunk.Chunk;
import tk.valoeghese.fc0.world.ChunkAccess;
import tk.valoeghese.fc0.world.TileAccess;
import tk.valoeghese.fc0.world.gen.ecozone.EcoZone;
import tk.valoeghese.fc0.world.gen.generator.Generator;
import tk.valoeghese.fc0.world.gen.generator.GeneratorSettings;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nullable;
import java.util.Random;

public abstract class WorldGen {
	public WorldGen(long seed, int plane) {
		this.noise = new Noise(new Random(seed));
		this.ridges = new RidgedNoise(new Random(seed + 12));
		this.sand = new Noise(new Random(seed - 29));
		this.ecoZone = new Noise(new Random(seed + 31));
		this.cavesHorizontal = new Noise(new Random(seed + 79));
		this.cavesMain = new Noise(new Random(seed - 79));
		this.plane = (double) plane / 3;
	}

	private final Noise noise;
	private final Noise ridges;
	private final Noise sand;
	private final Noise ecoZone;

	// Caves

	private final Noise cavesHorizontal;
	private final Noise cavesMain;

	private final double plane;

	public <T extends Chunk> T generateChunk(ChunkConstructor<T> constructor, ChunkAccess parent, int chunkX, int chunkZ, Random rand) {
		byte[] tiles = new byte[16 * 16 * TileAccess.WORLD_HEIGHT];
		byte[] meta = new byte[tiles.length];

		double[] noise = generateNoise(chunkX, chunkZ);
		shapeChunk(chunkX, chunkZ, noise, tiles, meta);
		carveCaves(chunkX, chunkZ, noise, tiles, meta);

		// TODO not cast like this. This is bad code. Should I just do away with the gajillion interfaces?
		return constructor.create((GameplayWorld<T>) parent, chunkX, chunkZ, tiles, meta, null);
	}

	private double[] generateNoise(int chunkX, int chunkZ) {
		int blockX = chunkX << 4;
		int blockZ = chunkZ << 4;

		double[] noise = new double[256];

		for (int x = 0; x < 16; ++x) {
			int totalX = x + blockX;

			for (int z = 0; z < 16; ++z) {
				int totalZ = z + blockZ;

				noise[x * 16 + z] = this.sampleHeight(totalX, totalZ);

			}
		}

		return noise;
	}

	private void shapeChunk(int chunkX, int chunkZ, double[] heightmap, byte[] tiles, byte[] meta) {
		int blockX = chunkX << 4;
		int blockZ = chunkZ << 4;

		for (int x = 0; x < 16; ++x) {
			int totalX = x + blockX;

			for (int z = 0; z < 16; ++z) {
				int totalZ = z + blockZ;
				EcoZone zone = getEcoZoneByPosition(totalX, totalZ);

				// ridges
				int height = (int) heightmap[x * 16 + z];

				int sandHeight = (int) (2.1 * sand.sample(totalX / 21.0, totalZ / 21.0));

				if (height >= TileAccess.WORLD_HEIGHT) {
					height = TileAccess.WORLD_HEIGHT - 1;
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
	}

	private void carveCaves(int chunkX, int chunkZ, double[] heightmap, byte[] tiles, byte[] meta) {
		int blockX = chunkX << 4;
		int blockZ = chunkZ << 4;

		for (int x = 0; x < 16; ++x) {
			int totalX = x + blockX;

			for (int z = 0; z < 16; ++z) {
				int totalZ = z + blockZ;

				double height = heightmap[x * 16 + z];

				double threshold = horizontalThresholdAt(cavesHorizontal.sample(totalX / 35.0, totalZ / 35.0));

				for (int y = 0; y < (int) height; y++) {
					if ((noiseFalloffAt(y, height) + (cavesMain.sample(totalX / 40.0, y / 20.0, totalZ / 40.0) * 2.5)) < threshold) {
						tiles[Chunk.index(x, y, z)] = Tile.AIR.id;
						meta[Chunk.index(x, y, z)] = 0;
					}
				}
			}
		}
	}

	private static double horizontalThresholdAt(double noise) {
		return (14.5 * noise * noise);
	}

	private static double noiseFalloffAt(int y, double maxHeight) {
		return (48.0 / y) - (48.0 / (y - maxHeight));
	}

	protected abstract double sampleHeight(double x, double z);

	public void populateChunk(GenWorld world, Chunk chunk, Random rand) {
		EcoZone zone = getEcoZoneByPosition(chunk.startX, chunk.startZ);

		for (Pair<Generator, GeneratorSettings> generator : zone.getGenerators()) {
			generator.getLeft().generate(world, generator.getRight(), chunk.startX, chunk.startZ, rand);
		}
	}

	public EcoZone getEcoZoneByPosition(double x, double z) {
		return getEcoZone(ecoZone.sample(x * 0.0012, z * 0.0012), ecoZone.sample(x * 0.002 + 4.08, z * 0.002));
	}

	public EcoZone getEcoZone(double temp, double humidity) {
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
			} else if (humidity < 0.15) {
				return EcoZone.TROPICAL_STEPPE;
			} else if (humidity < 0.2) {
				return EcoZone.TROPICAL_RAINFOREST_EDGE;
			} else {
				return EcoZone.TROPICAL_RAINFOREST;
			}
		}
	}

	public double sampleNoise(double x, double y) {
		return this.noise.sample(x, y, this.plane);
	}

	protected double sampleRidge(double x, double y) {
		return this.ridges.sample(x, y, this.plane);
	}

	@FunctionalInterface
	public interface ChunkConstructor<T extends Chunk> {
		T create(GameplayWorld<T> parent, int x, int z, byte[] tiles, byte[] meta, @Nullable int[] kingdoms);
	}

	/**
	 * World Generator for Earth.
	 */
	public static class Earth extends WorldGen {
		public Earth(long seed, int plane) {
			super(seed, plane);
		}

		@Override
		protected double sampleHeight(double x, double z) {
			// Stage one: sample continent shape
			double continent = 43 + 20 * this.sampleNoise((x / 810.0) - 0.3, (z / 810.0) - 0.3);

			// Stage two: sample mountains and hills
			double mountainDir = 1.0 + 0.5 * this.sampleNoise(x / 720.0, z / 720.0);
			double mountains = 45 + 68 * this.sampleRidge((x * mountainDir) / 410.0, (z / mountainDir) / 410.0);
			mountains += 36 * this.sampleRidge((x / 290.0) - 1, z / 290.0);

			double hills = 20 * this.sampleNoise(x / 90.0, z / 90.0) + 12 * this.sampleNoise(x / 32.0, z / 32.0);

			// Stage three: bias mountains and hills
			double bias = 0.5 + 0.5 * this.sampleNoise(x / 600.0, (z / 600.0) - 1);
			return continent + (bias * hills) + ((1.0 - bias) * mountains);
		}
	}
}
