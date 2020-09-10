package tk.valoeghese.fc0.world;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import tk.valoeghese.fc0.util.maths.ChunkPos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.util.maths.Vec2f;
import tk.valoeghese.fc0.world.entity.Entity;
import tk.valoeghese.fc0.world.gen.GenWorld;
import tk.valoeghese.fc0.world.gen.WorldGen;
import tk.valoeghese.fc0.world.gen.ecozone.EcoZone;
import tk.valoeghese.fc0.world.gen.kingdom.Kingdom;
import tk.valoeghese.fc0.world.gen.kingdom.Voronoi;
import tk.valoeghese.fc0.world.player.Player;
import tk.valoeghese.fc0.world.save.Save;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class GameplayWorld<T extends Chunk> implements LoadableWorld, ChunkAccess {
	public GameplayWorld(@Nullable Save save, long seed, int size, WorldGen.ChunkConstructor<T> constructor) {
		this.worldGen = new WorldGen.Earth(seed, 0);
		this.seed = seed;

		this.chunks = new Long2ObjectArrayMap<>();
		this.genRand = new Random(seed);
		this.constructor = constructor;
		this.save = save;

		this.skyLight = save == null ? 0 : save.loadedSkyLight;

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
	@Nullable
	private final Save save;
	private final ExecutorService chunkSaveExecutor = Executors.newSingleThreadExecutor();
	private final WorldGen worldGen;
	private byte skyLight;
	private List<Entity> entities = new ArrayList<>();
	private Int2ObjectMap<Kingdom> kingdomIdMap = new Int2ObjectArrayMap<>();

	private ChunkPos searchForSpawn() {
		int startCX = RANDOM.nextInt(16) - 8;
		int startCZ = RANDOM.nextInt(16) - 8;
		int chunkX = startCX;
		int chunkZ = startCZ;
		int height = 0;

		for (int xo = 0; xo < 4; ++xo) {
			int x = startCX + 2 * xo;

			for (int zo = 0; zo < 4; ++zo) {
				int z = startCZ + 2 * zo;

				Chunk c = this.loadChunk(x, z, ChunkLoadStatus.GENERATE);
				int y = c.getHeight(0, 0);

				if (y > 51) {
					return new ChunkPos(x, z);
				} else if (y > height) {
					height = y;
					chunkX = x;
					chunkZ = z;
				}
			}
		}

		return new ChunkPos(chunkX, chunkZ);
	}

	// Note: If a kingdom is generated in two locations
	// It could change the Voronoi location and thus city loc
	// perhaps causing world gen problems for partial cities and stuff
	// Perhaps I should just save this per world instead of genning at runtime
	// meh
	@Override
	public Kingdom kingdomById(int kingdom, int x, int z) {
		return this.kingdomIdMap.computeIfAbsent(kingdom, id -> new Kingdom(this, id, Voronoi.sample(x / Kingdom.SCALE, z / Kingdom.SCALE, (int) this.seed)));
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
			this.genRand.setSeed(seed + 134 * x + -529 * z);
			return this.worldGen.generateChunk(this.constructor, this, x, z, this.genRand);
		} else {
			return this.save.getOrCreateChunk(this.worldGen, this, x, z, this.constructor);
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

	public void assertSkylight(byte skyLight) {
		if (skyLight != this.skyLight) {
			this.skyLight = skyLight;

			for (Chunk chunk : this.chunks.values()) {
				if (chunk.status.isFull()) {
					chunk.assertSkylightSingle(skyLight);
				}
			}
		}
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
	public Chunk loadChunk(int x, int z, ChunkLoadStatus status) {
		if (status == ChunkLoadStatus.UNLOADED) {
			throw new RuntimeException("Cannot load a chunk with status \"Unloaded\"");
		}

		if (!this.isInWorld(x << 4, 50, z << 4)) {
			return null;
		}

		T result = this.getOrCreateChunk(x, z);

		switch (status) {
		case GENERATE:
			break;
		case RENDER: // actual specific RENDER case handling only happens client side
		case TICK: // render chunks are also ticking chunks
			if (!result.status.isFull()) {
				result.computeHeightmap();
			}

			if (result.needsLightingCalcOnLoad) {
				result.setSkylight(this.skyLight); // just in case
				result.updateLighting();
				result.needsLightingCalcOnLoad = false;
			} else if (!result.status.isFull()) { // if otherwise loading from older to a full status, make sure skyLight is correct
				result.assertSkylight(this.skyLight);
			}
		case POPULATE: // ticking chunks are also populated
			if (!result.populated) {
				result.populated = true;
				this.genRand.setSeed(this.seed + 134 * result.x + -529 * result.z + 127);
				this.worldGen.populateChunk(this.genWorld, result, this.genRand);
			}
			break;
		}

		result.status = result.status.upgrade(status);

		if (result != null) {
			this.chunks.put(key(x, z), result);
		}

		return result;
	}

	@Nullable
	private T accessChunk(int x, int z) {
		return this.chunks.get(key(x, z));
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
			if (c.getPos().manhattan(centrePos) > CHUNK_LOAD_DIST) {
				// prepare for chunk remove on-thread
				this.onChunkRemove(c);
				toWrite.add(c);
				this.chunks.remove(key(c.x, c.z));
				c.status = ChunkLoadStatus.UNLOADED;
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

	public void addEntity(Entity entity) {
		this.entities.add(entity);
	}

	@Override
	public void destroy() {
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

	public byte getSkyLight() {
		return this.skyLight;
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

	private static final int CHUNK_RENDER_DIST = 4;
	private static final int CHUNK_LOAD_DIST = CHUNK_RENDER_DIST + 3;
	private static final Random RANDOM = new Random();
}
