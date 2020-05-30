package tk.valoeghese.fc0.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import tk.valoeghese.fc0.util.maths.ChunkPos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.gen.GenWorld;
import tk.valoeghese.fc0.world.gen.WorldGen;
import tk.valoeghese.fc0.world.save.Save;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class GameplayWorld<T extends Chunk> implements LoadableWorld, ChunkAccess {
	public GameplayWorld(@Nullable Save save, long seed, int size, WorldGen.ChunkConstructor<T> constructor) {
		WorldGen.updateSeed(seed);
		this.seed = seed;

		this.chunks = new Long2ObjectArrayMap<>();
		this.genRand = new Random(seed);
		this.constructor = constructor;
		this.save = save;

		this.minBound = (-size + 1) << 4;
		this.maxBound = size << 4;
	}

	private final int minBound;
	private final int maxBound;
	private final Long2ObjectMap<T> chunks;
	private final Random genRand;
	private final long seed;
	private final GenWorld genWorld = new GeneratorWorldAccess();
	private final WorldGen.ChunkConstructor<T> constructor;
	@Nullable
	private final Save save;
	private final ExecutorService chunkSaveExecutor = Executors.newSingleThreadExecutor();

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

	public void generateSpawnChunks() {
		long time = System.currentTimeMillis();

		if (this.save != null) {
			System.out.println("Generating World Spawn.");
		}

		for (int cx = -3; cx <= 3; ++cx) {
			for (int cz = -3; cz <= 3; ++cz) {
				this.getChunk(cx, cz);
			}
		}

		if (this.save != null) {
			System.out.println("Generated World Spawn in " + (System.currentTimeMillis() - time) + "ms.");
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

		T result = this.getOrCreateChunk(x, z);

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

		if (result != null) {
			this.chunks.put(chunkKey(x, z), result);
		}

		return result;
	}

	@Nullable
	private T accessChunk(int x, int z) {
		return this.chunks.get(chunkKey(x, z));
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
		LoadableWorld.super.wgWriteTile(x, y, z, tile);
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

		if (player.chunk != null) {
			if (cPos.equals(player.chunk.getPos())) {
				return;
			}
		}

		if (this.isInWorld(pos.x, 50, pos.z)) {
			this.getChunk(cPos.x, cPos.z).updateChunkOf(player);
		} else if (player.chunk != null) {
			player.chunk.removePlayer(player);
			player.chunk = null;
		}

		this.chunkLoad(cPos);
	}

	@Override
	public void chunkLoad(ChunkPos centrePos) {
		List<Chunk> toWrite = new ArrayList<Chunk>();

		// prepare chunks to remove
		for (Chunk c : this.chunks.values()) {
			if (c.getPos().manhattan(centrePos) > CHUNK_LOAD_DIST) {
				// prepare for chunk remove on-thread
				this.onChunkRemove(c);
				toWrite.add(c);
				this.chunks.remove(chunkKey(c.x, c.z));
			}
		}

		// read new chunks
		for (int cx = centrePos.x - CHUNK_LOAD_DIST; cx <= centrePos.x + CHUNK_LOAD_DIST; ++cx) {
			for (int cz = centrePos.z - CHUNK_LOAD_DIST; cz <= centrePos.z + CHUNK_LOAD_DIST; ++cz) {
				int dist = centrePos.manhattan(cx, cz);

				if (dist > CHUNK_LOAD_DIST) {
					continue;
				}

				switch (dist) {
				case CHUNK_LOAD_DIST:
					this.loadChunk(cx, cz, ChunkLoadStatus.GENERATE);
					break;
				case CHUNK_LOAD_DIST - 1:
					this.loadChunk(cx, cz, ChunkLoadStatus.POPULATE);
					break;
				case CHUNK_LOAD_DIST - 2:
					this.loadChunk(cx, cz, ChunkLoadStatus.TICK);
					break;
				default:
					this.loadChunk(cx, cz, ChunkLoadStatus.RENDER);
					break;
				}
			}
		}

		if (this.save != null) {
			// write unnecessary chunks off-thread
			this.chunkSaveExecutor.execute(() -> {
				synchronized (this.save) {
					this.save.writeChunks(toWrite.iterator());
				}
			});
		}
	}

	protected abstract void onChunkRemove(Chunk c);

	@Override
	public void destroy() {
		this.chunkSaveExecutor.shutdown();
	}

	@Override
	public long getSeed() {
		return this.seed;
	}

	private class GeneratorWorldAccess implements GenWorld {
		@Override
		public boolean isInWorld(int x, int y, int z) {
			return GameplayWorld.this.isInWorld(x, y, z);
		}

		@Nullable
		@Override
		public Chunk getChunk(int x, int z) {
			return GameplayWorld.this.loadChunk(x, z, ChunkLoadStatus.GENERATE);
		}
	}

	public static final long chunkKey(int x, int z) {
		return (((long) x & 0x7FFFFFFF) << 32L) | ((long) z & 0x7FFFFFFF);
	}

	private static final int CHUNK_RENDER_DIST = 5;
	private static final int CHUNK_LOAD_DIST = CHUNK_RENDER_DIST + 3;
}
