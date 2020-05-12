package tk.valoeghese.fc0.world;

public final class WorldGen {
	public static Chunk generateChunk(int chunkX, int chunkZ) {
		byte[] tiles = new byte[16 * 16 * 128];

		for (int x = 0; x < 16; ++x) {
			for (int z = 0; z < 16; ++z) {
				for (int y = 0; y < 50; ++y) {
					tiles[Chunk.index(x, y, z)] = Tile.STONE.id;
				}
			}
		}

		return new Chunk(chunkX, chunkZ, tiles);
	}
}
