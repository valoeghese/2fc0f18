package tk.valoeghese.fc0.world;

import tk.valoeghese.fc0.util.Noise;
import tk.valoeghese.fc0.util.RidgedNoise;

import java.util.Random;

public final class WorldGen {
	public static Chunk generateChunk(int chunkX, int chunkZ, long seed, Random rand) {
		if (noise == null || seed != cachedSeed) {
			noise = new Noise(new Random(seed));
			ridges = new RidgedNoise(new Random(seed + 12));
		}

		byte[] tiles = new byte[16 * 16 * 128];
		int blockX = chunkX << 4;
		int blockZ = chunkZ << 4;

		for (int x = 0; x < 16; ++x) {
			int totalX = x + blockX;

			for (int z = 0; z < 16; ++z) {
				int totalZ = z + blockZ;
				int height = (int) (3.0 * noise.sample(totalX / 24.0, totalZ / 24.0))
						+ (int) (9.5 * ridges.sample(totalX / 75.0, totalZ / 75.0))
						+ 50;

				if (noise.sample(totalX / 86.0, totalZ / 86.0) > noise.sample((totalX + 121) / 66.5, (totalZ + 121) / 66.5)) {
					height += 3;
				}

				for (int y = 0; y < height; ++y) {
					tiles[Chunk.index(x, y, z)] = y == height - 1 ? Tile.GRASS.id : Tile.STONE.id;
				}
			}
		}

		return new Chunk(chunkX, chunkZ, tiles);
	}

	private static Noise noise;
	private static Noise ridges;
	private static long cachedSeed = 0;
}
