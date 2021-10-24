package tk.valoeghese.fc0.world;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import tk.valoeghese.fc0.Game2fc;
import tk.valoeghese.fc0.util.maths.ChunkPos;
import tk.valoeghese.fc0.util.maths.MathsUtils;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.util.maths.Vec2f;
import tk.valoeghese.fc0.world.entity.Entity;
import tk.valoeghese.fc0.world.gen.GenWorld;
import tk.valoeghese.fc0.world.gen.WorldGen;
import tk.valoeghese.fc0.world.gen.ecozone.EcoZone;
import tk.valoeghese.fc0.world.kingdom.Kingdom;
import tk.valoeghese.fc0.world.kingdom.Voronoi;
import tk.valoeghese.fc0.world.player.Player;
import tk.valoeghese.fc0.world.save.ChunkLoadingAccess;
import tk.valoeghese.fc0.world.save.Save;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.joml.Math.sin;

public abstract class GameplayWorld<T extends Chunk> implements LoadableWorld, ChunkLoadingAccess<T> {
	public GameplayWorld(Save save, long seed, int size, WorldGen.ChunkConstructor<T> constructor) {
		this.worldGen = new WorldGen.Earth(seed, 0);
		this.seed = seed;

		this.chunks = new Long2ObjectArrayMap<>();
		this.genRand = new Random(seed);
		this.constructor = constructor;
		this.save = save;

		this.minBound = (-size + 1) << 4;
		this.maxBound = size << 4;

		RANDOM.setSeed(seed);
		this.spawnChunk = save == null ? new ChunkPos(0, 0) : this.searchForSpawn();
	}

	private final int minBound;
	private final int maxBound;
	private final ChunkPos spawnChunk;
	private final Long2ObjectMap<T> chunks;
	private final Random genRand;
	private final long seed;
	private final GenWorld genWorld = new GeneratorWorldAccess();
	private final WorldGen.ChunkConstructor<T> constructor;
	private final Save save;
	private final WorldGen worldGen;
	private List<Entity> entities = new ArrayList<>();
	private Int2ObjectMap<Kingdom> kingdomIdMap = new Int2ObjectArrayMap<>();

	private ChunkPos searchForSpawn() {
		return new ChunkPos(0, 0); // TODO adapt for new chunk loading
	}

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

	private T getOrCreateChunk(int x, int z) {
		T result = this.accessChunk(x, z);

		if (result != null) {
			return result;
		}

		if (this.save == null) {

		} else {
			return this.save.loadChunk(this.worldGen, this, x, z, this.constructor);
		}
	}

	public void generateSpawnChunks(ChunkPos around) {
		// TODO fix
		/*long time = System.currentTimeMillis();

		if (this.save != null) {
			System.out.println("Generating World Spawn.");
		}

		for (int cx = around.x + -3; cx <= around.x + 3; ++cx) {
			for (int cz = around.z + -3; cz <= around.z + 3; ++cz) {
				this.loadChunk(cx, cz, ChunkLoadStatus.TICK);
			}
		}

		if (this.save != null) {
			System.out.println("Generated World Spawn in " + (System.currentTimeMillis() - time) + "ms.");
		}*/
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

		this.getOrCreateChunk(x, z);
		return true;
	}

	@Nullable
	private T accessChunk(int x, int z) {
		return this.chunks.get(key(x, z));
	}

	@Override
	@Nullable
	public Chunk getChunk(int x, int z) {
		return this.accessChunk(x, z);
	}

	@Override
	public void addLoadedChunk(Chunk chunk, ChunkLoadStatus status) {
		switch (status) {
		case GENERATE:
			break;
		case RENDER: // actual specific RENDER case handling only happens client side
		case TICK: // render chunks are also ticking chunks
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

		chunk.status = chunk.status.upgrade(status);
		this.chunks.put(key(chunk.x, chunk.z), (T)chunk);
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

	@Nullable
	@Override
	public Chunk getFullChunk(int x, int z) {
		Chunk c = this.accessChunk(x, z);

		if (c == null) {
			return null;
		}

		if (c.status.isFull()) {
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
			// ensure rendered
			this.loadChunk(cPos.x, cPos.z, ChunkLoadStatus.RENDER).updateChunkOf(player);
		} else if (player.chunk != null) {
			player.chunk.removePlayer(player);
			player.chunk = null;
		}

		this.chunkLoad(cPos);
	}

	@Override
	public void chunkLoad(ChunkPos centrePos) {
		List<Chunk> toWrite = new ArrayList<>();

		// prepare chunks to remove
		for (Chunk c : this.chunks.values()) {
			// TODO make this check better (update 11/10/2021: Have I made it better?)
			// We keep chunks for a longer distance than we load them because we are based
			if (c != null && c.getPos().manhattan(centrePos) > CHUNK_KEEP_DIST) {
				// prepare for chunk remove on-thread
				this.onChunkRemove(c);
				toWrite.add(c);
				this.chunks.remove(key(c.x, c.z));
				c.status = ChunkLoadStatus.UNLOADED;
			}
		}

		// read new chunks
		for (int cx = centrePos.x - CHUNK_TICK_DIST; cx <= centrePos.x + CHUNK_TICK_DIST; ++cx) {
			for (int cz = centrePos.z - CHUNK_TICK_DIST; cz <= centrePos.z + CHUNK_TICK_DIST; ++cz) {
				int dist = centrePos.manhattan(cx, cz);

				if (dist > CHUNK_TICK_DIST) {
					continue;
				}

				this.loadChunk(cx, cz, dist == CHUNK_TICK_DIST ? ChunkLoadStatus.TICK : ChunkLoadStatus.RENDER);
			}
		}

		if (this.save != null && !toWrite.isEmpty()) {
			// write unnecessary chunks off-thread
			this.chunkSaveExecutor.execute(() -> {
				synchronized (this.save) {
					this.save.writeChunks(toWrite.iterator());
				}
			});
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

		this.chunkSaveExecutor.shutdown();
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

	// TODO when lighting is a shader, don't rebuild the model every time and switch this to float
	public int getSkyLight() {
		return MathsUtils.clamp(MathsUtils.floor(Game2fc.SKY_CHANGE_RATE * sin((float) Game2fc.getInstance().time / 9216.0f) + 7.5f), 0, 10);
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

		@Nullable
		@Override
		public Chunk getChunk(int x, int z) {
			Chunk result = GameplayWorld.this.loadChunk(x, z, ChunkLoadStatus.GENERATE);

			/*if (result != null) {
				System.out.println(result.getPos());
			}*/

			return result;
		}

		@Override
		public void wgWriteTile(int x, int y, int z, byte tile) {
			if (Tile.BY_ID[tile].canPlaceAt(this, x, y, z)) {
				GenWorld.super.wgWriteTile(x, y, z, tile);
			}
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

	private static final int CHUNK_TICK_DIST = 5;
	private static final int CHUNK_KEEP_DIST = CHUNK_TICK_DIST + 2;
	private static final Random RANDOM = new Random();
}
