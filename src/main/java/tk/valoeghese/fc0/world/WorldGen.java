package tk.valoeghese.fc0.world;

import java.util.Random;

public final class WorldGen {
	public static Chunk generateChunk(int chunkX, int chunkZ, Random rand) {
		byte[] tiles = new byte[16 * 16 * 128];
		int blockX = chunkX << 4;
		int blockZ = chunkZ << 4;

		for (int x = 0; x < 16; ++x) {
			int totalX = x + blockZ;

			for (int z = 0; z < 16; ++z) {
				int totalZ = z + blockZ;
				
				for (int y = 0; y < 50 + rand.nextInt(3); ++y) {
					tiles[Chunk.index(x, y, z)] = Tile.STONE.id;
				}
			}
		}

		return new Chunk(chunkX, chunkZ, tiles);
	}
}
