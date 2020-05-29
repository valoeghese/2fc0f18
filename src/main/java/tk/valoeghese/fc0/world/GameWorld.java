package tk.valoeghese.fc0.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import tk.valoeghese.fc0.util.maths.ChunkPos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.gen.GenWorld;
import tk.valoeghese.fc0.world.gen.WorldGen;
import tk.valoeghese.fc0.world.save.Save;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Random;

public abstract class GameWorld<T extends Chunk> implements World, ChunkAccess {
	public GameWorld(@Nullable Save save, long seed, int size, WorldGen.ChunkConstructor<T> constructor) {
		WorldGen.updateSeed(seed);
		this.seed = seed;
		this.offset = size - 1;
		this.diameter = 1 + this.offset * 2;
		long time = System.currentTimeMillis();

		if (save != null) {
			System.out.println("Generating World.");
		}

		this.chunks = new Long2ObjectArrayMap<>();
		this.genRand = new Random(seed);
		this.constructor = constructor;
		this.save = save;

		for (int x = -size + 1; x < size; ++x) {
			for (int z = -size + 1; z < size; ++z) {
				T chunk = this.getOrCreateChunk(x, z);
				this.chunks.put((x + this.offset) * this.diameter + z + this.offset, chunk);
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
	private final Long2ObjectMap<T> chunks;
	private final Random genRand;
	private final long seed;
	private final GenWorld genWorld = new GeneratorWorldAccess();
	private final WorldGen.ChunkConstructor<T> constructor;
	@Nullable
	private final Save save;

	private T getOrCreateChunk(int x, int z) {
		T result = this.accessChunk(x, z);

		if (result != null) {
			return result;
		}

		if (save == null) {
			this.genRand.setSeed(seed + 134 * x + -529 * z);
			return WorldGen.generateChunk(this.constructor, this, x, z, seed, this.genRand);
		} else {
			return this.save.getOrCreateChunk(this, x, z, this.constructor);
		}
	}

	public void populateChunks() {
		long time = System.currentTimeMillis();

		if (this.seed != 0) {
			System.out.println("Populating World.");
		}

		for (Chunk chunk : this.chunks.values()) {
			if (!chunk.populated) {
				this.genRand.setSeed(this.seed + 134 * chunk.x + -529 * chunk.z + 127);
				WorldGen.populateChunk(this.genWorld, chunk, this.genRand);
				chunk.populated = true;
			}
		}

		if (this.seed != 0) {
			System.out.println("Populated World in " + (System.currentTimeMillis() - time) + "ms.");
		}
	}

	public Iterator<T> getChunks() {
		return this.chunks.values().iterator();
	}

	@Override
	@Nullable
	public Chunk loadChunk(int x, int z, ChunkLoadStatus status) {
		if (!this.isInWorld(x << 4, 50, z << 4)) {
			return null;
		}

		Chunk result = this.getOrCreateChunk(x, z);

		switch (status) {
		case GENERATE:
			break;
		case RENDER: // actual specific RENDER case handling only happens client side
		case TICK: // render chunks are also ticking chunks
		case POPULATE: // ticking chunks are also populated
			if (!result.populated) {
				this.genRand.setSeed(this.seed + 134 * result.x + -529 * result.z + 127);
				WorldGen.populateChunk(this.genWorld, result, this.genRand);
				result.populated = true;
			}
			break;
		}

		return result;
	}

	@Nullable
	private T accessChunk(int x, int z) {
		return this.chunks.get((x + this.offset) * this.diameter + z + this.offset);
	}

	@Override
	@Nullable
	public Chunk getChunk(int x, int z) {
		return this.loadChunk(x, z, ChunkLoadStatus.POPULATE);
	}

	@Nullable
	@Override
	public Chunk getRenderChunk(int x, int z) {
		Chunk c = this.accessChunk(x, z);

		if (c == null) {
			return null;
		}

		if (c.render) {
			return c;
		}

		return null;
	}

	@Override
	public void writeTile(int x, int y, int z, byte tile) {
		World.super.wgWriteTile(x, y, z, tile);
	}

	@Override
	public void wgWriteTile(int x, int y, int z, byte tile) {
		this.genWorld.wgWriteTile(x, y, z, tile);
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
			this.getChunk(cPos.x, cPos.z).updateChunkOf(player);
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

	private class GeneratorWorldAccess implements GenWorld {
		@Override
		public boolean isInWorld(int x, int y, int z) {
			return GameWorld.this.isInWorld(x, y, z);
		}

		@Nullable
		@Override
		public Chunk getChunk(int x, int z) {
			return GameWorld.this.loadChunk(x, z, ChunkLoadStatus.GENERATE);
		}
	}
}
