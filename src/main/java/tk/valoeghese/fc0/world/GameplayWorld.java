package tk.valoeghese.fc0.world;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import tk.valoeghese.fc0.Game2fc;
import tk.valoeghese.fc0.util.OrderedList;
import tk.valoeghese.fc0.util.Synchronise;
import tk.valoeghese.fc0.util.maths.ChunkPos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.util.maths.Vec2f;
import tk.valoeghese.fc0.world.chunk.Chunk;
import tk.valoeghese.fc0.world.chunk.OverflowChunk;
import tk.valoeghese.fc0.world.chunk.TileWriter;
import tk.valoeghese.fc0.world.chunk.ChunkLoadStatus;
import tk.valoeghese.fc0.world.entity.Entity;
import tk.valoeghese.fc0.world.gen.GenWorld;
import tk.valoeghese.fc0.world.gen.WorldGen;
import tk.valoeghese.fc0.world.gen.ecozone.EcoZone;
import tk.valoeghese.fc0.world.kingdom.Kingdom;
import tk.valoeghese.fc0.world.kingdom.Voronoi;
import tk.valoeghese.fc0.world.player.Player;
import tk.valoeghese.fc0.world.save.ChunkLoadingAccess;
import tk.valoeghese.fc0.world.save.SaveLike;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.joml.Math.sin;

public abstract class GameplayWorld<T extends Chunk> implements LoadableWorld, ChunkLoadingAccess<T> {
	public GameplayWorld(SaveLike save, long seed, int size, WorldGen.ChunkConstructor<T> constructor) {
		this.worldGen = new WorldGen.Earth(seed, 0);
		this.seed = seed;

		this.chunks = new Long2ObjectArrayMap<>();
		this.overflowChunks = new Long2ObjectArrayMap<>();
		this.scheduledTasks = new Long2ObjectArrayMap<>();

		this.genRand = new Random(seed);
		this.constructor = constructor;
		this.save = save;

		this.minBound = (-size + 1) << 4;
		this.maxBound = size << 4;

		RANDOM.setSeed(seed);
		this.spawnChunk = new ChunkPos(0, 0);
		this.updateSkylight();
	}

	private final int minBound;
	private final int maxBound;

	// Chunk Stuff
	private final ChunkPos spawnChunk;
	protected final Long2ObjectMap<T> chunks;
	private final Long2ObjectMap<OverflowChunk> overflowChunks;
	@Synchronise
	private final Long2ObjectMap<List<ScheduledTask>> scheduledTasks;

	private final Random genRand;
	private final long seed;
	private final GenWorld genWorld = new GeneratorWorldAccess();
	private final WorldGen.ChunkConstructor<T> constructor;
	private final SaveLike save;
	private final WorldGen worldGen;
	private List<Entity> entities = new ArrayList<>();
	private Int2ObjectMap<Kingdom> kingdomIdMap = new Int2ObjectArrayMap<>();
	private final LongSet loading = new LongArraySet();
	private float skylight = -1.0f;

	// Note: If a kingdom is generated in two locations
	// It could change the Voronoi location and thus city loc
	// perhaps causing world gen problems for partial cities and stuff
	// Perhaps I should just save this per world instead of genning at runtime
	// meh
	@Override
	public Kingdom kingdomById(int kingdom, int x, int z) {
		return this.kingdomIdMap.computeIfAbsent(kingdom, id -> new Kingdom(this, id, Voronoi.sampleVoronoi(x / Kingdom.SCALE, z / Kingdom.SCALE, (int) this.seed, 0.5f)));
	}

	public Kingdom kingdomById(Vec2f sample) {
		return this.kingdomIdMap.computeIfAbsent(sample.id(), id -> new Kingdom(this, id, sample));
	}

	public Iterator<T> getChunks() {
		return this.chunks.values().iterator();
	}

	@Override
	public double sampleNoise(double x, double y) {
		return this.worldGen.sampleNoise(x, y);
	}

	public EcoZone getEcozone(double x, double z) {
		return this.worldGen.getEcoZoneByPosition(x, z);
	}

	@Override
	@Nullable
	public boolean loadChunk(int x, int z, ChunkLoadStatus status) {
		if (status == ChunkLoadStatus.UNLOADED) {
			throw new RuntimeException("Cannot load a chunk with status \"Unloaded\"");
		}

		if (!this.isInWorld(x << 4, 50, z << 4)) {
			return false;
		}

		T existing = this.getChunk(x, z);

		if (existing == null) {
			long key = key(x, z);

			if (!this.loading.contains(key)) {
				this.loading.add(key);
				this.save.loadChunk(this.worldGen, this, x, z, this.constructor, status);
			}
		} else {
			this.addUpgradedChunk(existing, status);
		}
		return true;
	}

	@Override
	@Nullable
	public T getChunk(int x, int z) {
		return this.chunks.get(key(x, z));
	}

	@Nullable
	public T getChunk(ChunkPos pos) {
		return this.chunks.get(key(pos.x, pos.z));
	}

	@Override
	public TileWriter getDelayedLoadChunk(int x, int z) {
		Chunk result = this.getChunk(x, z);

		if (result == null) {
			// this is possible because operations on the DelayedLoadChunk SHOULD be on the main thread
			// SHOULD
			// because population is on the main thread and I think we only use it there
			long key = key(x, z);
			OverflowChunk overflow = this.overflowChunks.get(key);

			if (overflow == null) {
				overflow = new OverflowChunk(x, z);
				this.overflowChunks.put(key, overflow);
				this.loadChunk(x, z, ChunkLoadStatus.GENERATE);
			}

			return overflow;
		} else {
			return result;
		}
	}

	@Override
	public void scheduleForChunk(long chunkPos, Consumer<Chunk> callback, String taskName) {
		Chunk chunk = this.chunks.get(chunkPos);

		// in case a task is scheduled off thread. Otherwise we might have a race condition where some code is not called.
		// Currently we only do this from the main thread but this is a future precaution for potential use offthread.
		synchronized (this.scheduledTasks) {
			if (chunk == null) {
				this.scheduledTasks.computeIfAbsent(chunkPos, n -> new ArrayList<>()).add(new ScheduledTask(callback, taskName, System.currentTimeMillis() + 5000 /*5 seconds too long*/));
				return; // Early Return so the callback accept is not in the synchronise block
			}
		}

		// if already loaded, just run it (should this be forced on main thread by scheduling if not??)
		callback.accept(chunk);
	}

	// this is only done on main thread.
	@Override
	public void addUpgradedChunk(final T chunk, ChunkLoadStatus status) {
		long key = key(chunk.x, chunk.z);
		OverflowChunk overflow = this.overflowChunks.remove(key);

		if (overflow != null) {
			chunk.addOverflow(overflow);
		}

		// make populate able to access the full chunk.
		this.chunks.put(key(chunk.x, chunk.z), (T)chunk);

		// TODO is this necessary?
		if (chunk.status == ChunkLoadStatus.UNLOADED) {
			chunk.status = chunk.populated ? ChunkLoadStatus.POPULATE : ChunkLoadStatus.GENERATE;
		}

		switch (status) {
		case GENERATE:
			break;
		case RENDER: // actual specific RENDER case handling only happens client side
		case LIGHTING: // render chunks are also ticking chunks
			if (chunk.needsLightingCalcOnLoad) {
				chunk.updateLighting();
				chunk.needsLightingCalcOnLoad = false;
			}
		case POPULATE: // ticking chunks are also populated
			if (!chunk.populated) {
				chunk.populated = true;
				this.genRand.setSeed(this.seed + 134 * chunk.x + -529 * chunk.z + 127);
				this.worldGen.populateChunk(this.genWorld, chunk, this.genRand);
				chunk.computeHeightmap();
			}
			break;
		}

		// if moving to a full status
		if (status.isFull() && !chunk.status.isFull()) {
			chunk.onChunkLightingPhase();
		}

		// upgrade just in case :tm:
		chunk.status = chunk.status.upgrade(status);
		this.loading.remove(key);

		synchronized (this.scheduledTasks) {
			List<ScheduledTask> tasks = this.scheduledTasks.remove(key);

			if (tasks != null) {
				long execStartTime = System.currentTimeMillis();

				for (ScheduledTask task : tasks) {
					if (execStartTime > task.tooLongTime) {
						// yeah giving a number for a chunk and not specifying it's a key could be confusing to users
						// whatever it's simple enough to understand each chunk position has a different number, it's fine
						// plus who's actually going to report this issue lol no one reads console unless they're a dev
						System.out.println("Scheduled Chunk Task " + task.name + " at chunk " + key + " took too long (greater than 5 seconds) before starting execution!");
					}
					task.task.accept(chunk);
				}
			}
		}
	}

	@Nullable
	@Override
	public Chunk getFullChunk(int x, int z) {
		Chunk c = this.getChunk(x, z);

		if (c == null) {
			return null;
		}

		if (c.status.isFull()) {
			return c;
		}

		return null;
	}

	@Override
	public boolean isInWorld(int x, int y, int z) {
		return x >= this.minBound && x < this.maxBound && z >= this.minBound && z < this.maxBound && y >= 0 && y < WORLD_HEIGHT;
	}

	@Override
	public void updateChunkOf(Player player) {
		TilePos pos = player.getNextTilePos();
		ChunkPos cPos = pos.toChunkPos();

		if (player.lastChunkloadChunk != null) {
			if (cPos.equals(player.lastChunkloadChunk)) {
				return;
			}
		}

		// chunk load first since updateChunkOf should avoid null chunks where possible (unless you're leaving the world in which case the game will probably break anyway)
		player.lastChunkloadChunk = cPos;
		this.chunkLoad(cPos);

		if (this.isInWorld(pos.x, 50, pos.z)) {
			// ensure rendered
			this.scheduleForChunk(key(cPos.x, cPos.z), c -> c.updateChunkOf(player), "updatePlayerChunk");
		} else if (player.chunk != null) {
			player.chunk.removePlayer(player);
			player.chunk = null;
		}
	}

	@Override
	public void chunkLoad(ChunkPos centrePos) {
		List<Chunk> toWrite = new ArrayList<>();

		// prepare chunks to remove
		for (Chunk c : this.chunks.values()) {
			// We keep chunks for a longer distance than we load them because we are based
			if (c != null && c.getPos().manhattan(centrePos) > chunkKeepDist) {
				// prepare for chunk remove on-thread
				this.onChunkRemove(c);
				toWrite.add(c);
				// I mean, scheduled tasks shouldn't have an entry at the key at this point, and neither should overflows have an entry
				// so remove(key) should not be necessary for those maps
				this.chunks.remove(key(c.x, c.z));
				c.status = ChunkLoadStatus.UNLOADED;
			}
		}

		// order chunks to load
		OrderedList<ChunkPos> render = new OrderedList<>(centrePos::manhattan);
		List<ChunkPos> tick = new ArrayList<>();

		// read new chunks
		for (int cx = centrePos.x - chunkTickDist; cx <= centrePos.x + chunkTickDist; ++cx) {
			for (int cz = centrePos.z - chunkTickDist; cz <= centrePos.z + chunkTickDist; ++cz) {
				int dist = centrePos.manhattan(cx, cz);

				if (dist > chunkTickDist) {
					continue;
				}

				(dist == chunkTickDist ? tick : render).add(new ChunkPos(cx, cz));
			}
		}

		for (ChunkPos pos : render) {
			this.loadChunk(pos.x, pos.z, ChunkLoadStatus.RENDER);
		}
		for (ChunkPos pos : tick) {
			this.loadChunk(pos.x, pos.z, ChunkLoadStatus.LIGHTING); // Needs to be lighting to ensure outer render chunks are lit properly
		}

		if (!toWrite.isEmpty()) {
			this.save.writeChunks(toWrite.iterator());
		}
	}

	protected abstract void onChunkRemove(Chunk c);

	public void addEntity(Entity entity) {
		this.entities.add(entity);
	}

	@Override
	public void destroy() {
		for (Chunk c : this.chunks.values()) {
			c.destroy();
		}
	}

	@Override
	public long getSeed() {
		return this.seed;
	}

	@Override
	public ChunkPos getSpawnPos() {
		return this.spawnChunk;
	}

	@Override
	public GameplayWorld<?> getGameplayWorld() {
		return this;
	}

	@Override
	public int getHeight(int x, int z, Predicate<Tile> solid) {
		return this.getChunk(x >> 4, z >> 4).getHeight(x & 0xF, z & 0xF, solid);
	}

	@Override
	public Kingdom getKingdom(int x, int z) {
		return this.getChunk(x >> 4, z >> 4).getKingdom(x & 0xF, z & 0xF);
	}

	@Override
	public int getKingdomId(int x, int z) {
		return this.getChunk(x >> 4, z >> 4).getKingdomId(x & 0xF, z & 0xF);
	}

	// TODO when lighting is a shader, don't rebuild the model every time
	public boolean updateSkylight() {
		float newSkylight = 0.125f * (float) (int) (8 * Game2fc.getInstance().calculateLighting());
		boolean result = newSkylight != this.skylight;
		this.skylight = newSkylight;
		return result;
	}

	public float getSkyLight() {
		return this.skylight;
	}

	public List<Entity> getEntities(int x, int z, int radius) {
		List<Entity> result = new ArrayList<>();

		for (Entity entity : this.entities) {
			if (entity.getTilePos().horizontalManhattanDist(x, z) <= radius) {
				result.add(entity);
			}
		}

		return result;
	}

	public List<Entity> getAllEntities() {
		return new ArrayList<>(this.entities);
	}

	private class GeneratorWorldAccess implements GenWorld {
		@Override
		public boolean isInWorld(int x, int y, int z) {
			return GameplayWorld.this.isInWorld(x, y, z);
		}

		@Override
		public TileWriter getDelayedLoadChunk(int x, int z) {
			return GameplayWorld.this.getDelayedLoadChunk(x, z);
		}

		@Override
		public int getHeight(int x, int z, Predicate<Tile> solid) {
			return GameplayWorld.this.getChunk(x >> 4, z >> 4).getHeight(x & 0xF, z & 0xF, solid);
		}

		@Override
		public Kingdom getKingdom(int x, int z) {
			return GameplayWorld.this.getChunk(x >> 4, z >> 4).getKingdom(x & 0xF, z & 0xF);
		}

		@Override
		public double sampleNoise(double x, double y) {
			return GameplayWorld.this.sampleNoise(x, y);
		}

		@Override
		public long getSeed() {
			return GameplayWorld.this.getSeed();
		}

		@Override
		public GameplayWorld<?> getGameplayWorld() {
			return GameplayWorld.this;
		}
	}

	public static long key(int x, int z) {
		return (((long) x & 0x7FFFFFFF) << 32L) | ((long) z & 0x7FFFFFFF);
	}

	public static int getChunkTickDist() {
		return chunkTickDist;
	}

	public static void setChunkTickDist(int chunkTickDist) {
		GameplayWorld.chunkTickDist = chunkTickDist;
		chunkKeepDist = chunkTickDist + 2;
	}

	private static int chunkTickDist = 8;
	private static int chunkKeepDist = chunkTickDist + 2;
	private static final Random RANDOM = new Random();

	private static record ScheduledTask(Consumer<Chunk> task, String name, long tooLongTime) {}
}
