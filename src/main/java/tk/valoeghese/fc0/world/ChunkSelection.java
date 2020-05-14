package tk.valoeghese.fc0.world;

import tk.valoeghese.fc0.util.TilePos;

import java.util.Random;

public class ChunkSelection implements World {
	public ChunkSelection(long seed) {
		this.offset = SIZE - 1;
		this.diameter = 1 + this.offset * 2;
		this.chunks = new Chunk[this.diameter * this.diameter];
		Random rand = new Random(seed);

		for (int x = -SIZE + 1; x < SIZE; ++x) {
			for (int z = -SIZE + 1; z < SIZE; ++z) {
				this.chunks[(x + this.offset) * this.diameter + z + this.offset] = WorldGen.generateChunk(x, z, rand);
			}
		}

		this.minBound = (-SIZE + 1) << 4;
		this.maxBound = SIZE << 4;
	}

	private final int offset;
	private final int diameter;
	private final int minBound;
	private final int maxBound;
	private final Chunk[] chunks;

	public Chunk getChunk(int x, int z) {
		return this.chunks[(x + this.offset) * this.diameter + z + this.offset];
	}

	public Chunk[] getChunks() {
		return this.chunks;
	}

	@Override
	public byte readTile(int x, int y, int z) {
		return this.getChunk(x >> 4, z >> 4).readTile(x & 0xF, y, z & 0xF);
	}

	@Override
	public void writeTile(int x, int y, int z, byte tile) {
		this.getChunk(x >> 4, z >> 4).writeTile(x & 0xF, y, z & 0xF, tile);
	}

	@Override
	public boolean isInWorld(TilePos pos) {
		return pos.x >= this.minBound && pos.x < this.maxBound && pos.z >= this.minBound && pos.z < this.maxBound && pos.y >= 0 && pos.y < 128;
	}

	private static final int SIZE = 6;
}
