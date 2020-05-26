package tk.valoeghese.fc0.world;

import tk.valoeghese.fc0.util.OrderedList;
import tk.valoeghese.fc0.util.maths.ChunkPos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.gen.WorldGen;
import tk.valoeghese.fc0.world.save.Save;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public abstract class ChunkSelection<T extends Chunk> implements World, ChunkAccess {
	public ChunkSelection(@Nullable Save save, long seed, int size, WorldGen.ChunkConstructor<T> constructor, IntFunction<T[]> arraySupplier) {
		WorldGen.updateSeed(seed);
		this.seed = seed;
		this.offset = size - 1;
		this.diameter = 1 + this.offset * 2;
		long time = System.currentTimeMillis();

		if (save != null) {
			System.out.println("Generating World.");
		}

		this.chunks = arraySupplier.apply(this.diameter * this.diameter);
		this.genRand = new Random(seed);

		for (int x = -size + 1; x < size; ++x) {
			for (int z = -size + 1; z < size; ++z) {
				T chunk;

				if (save == null) {
					this.genRand.setSeed(seed + 134 * x + -529 * z);
					chunk = WorldGen.generateChunk(constructor, this, x, z, seed, this.genRand);
				} else {
					chunk = save.getOrCreateChunk(this, x, z, constructor);
				}

				this.chunks[(x + this.offset) * this.diameter + z + this.offset] = chunk;
			}
		}

		if (save != null) {
			System.out.println("Generated World in " + (System.currentTimeMillis() - time) + "ms.");
		}

		this.minBound = (-size + 1) << 4;
		this.maxBound = size << 4;
	}

	private final int offset;
	private final int diameter;
	private final int minBound;
	private final int maxBound;
	private final T[] chunks;
	private final Random genRand;
	private final long seed;

	public void populateChunks() {
		long time = System.currentTimeMillis();

		if (this.seed != 0) {
			System.out.println("Populating World.");
		}

		for (Chunk chunk : this.chunks) {
			if (!chunk.populated) {
				this.genRand.setSeed(this.seed + 134 * chunk.x + -529 * chunk.z + 127);
				WorldGen.populateChunk(this, chunk, this.genRand);
				chunk.populated = true;
			}
		}

		if (this.seed != 0) {
			System.out.println("Populated World in " + (System.currentTimeMillis() - time) + "ms.");
		}
	}

	public T[] getChunks() {
		return this.chunks;
	}

	@Override
	@Nullable
	public Chunk getChunk(int x, int z) {
		if (!this.isInWorld(x << 4, 50, z << 4)) {
			return null;
		}

		return this.getChunkDirect(x, z);
	}

	public Chunk getChunkDirect(int x, int z) {
		return this.chunks[(x + this.offset) * this.diameter + z + this.offset];
	}

	@Override
	public byte readTile(int x, int y, int z) {
		return this.getChunkDirect(x >> 4, z >> 4).readTile(x & 0xF, y, z & 0xF);
	}

	@Override
	public byte readMeta(int x, int y, int z) {
		return this.getChunkDirect(x >> 4, z >> 4).readMeta(x & 0xF, y, z & 0xF);
	}

	@Override
	public void writeTile(int x, int y, int z, byte tile) {
		this.getChunkDirect(x >> 4, z >> 4).writeTile(x & 0xF, y, z & 0xF, tile);
	}

	@Override
	public void writeMeta(int x, int y, int z, byte meta) {
		this.getChunkDirect(x >> 4, z >> 4).writeMeta(x & 0xF, y, z & 0xF, meta);
	}

	@Override
	public int getHeight(int x, int z, Predicate<Tile> solid) {
		return this.getChunkDirect(x >> 4, z >> 4).getHeight(x & 0xF, z & 0xF, solid);
	}

	@Override
	public boolean isInWorld(int x, int y, int z) {
		return x >= this.minBound && x < this.maxBound && z >= this.minBound && z < this.maxBound && y >= 0 && y < WORLD_HEIGHT;
	}

	@Override
	public void updateChunkOf(Player player) {
		TilePos pos = player.getTilePos();
		ChunkPos cPos = pos.toChunkPos();

		if (this.isInWorld(pos.x, 50, pos.z)) {
			this.getChunkDirect(cPos.x, cPos.z).updateChunkOf(player);
		} else if (player.chunk != null) {
			player.chunk.removePlayer(player);
			player.chunk = null;
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public long getSeed() {
		return this.seed;
	}
}
