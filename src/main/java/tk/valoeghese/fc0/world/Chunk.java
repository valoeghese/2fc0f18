package tk.valoeghese.fc0.world;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import tk.valoeghese.fc0.Game2fc;
import tk.valoeghese.fc0.util.maths.ChunkPos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.gen.WorldGen;
import tk.valoeghese.fc0.world.kingdom.Kingdom;
import tk.valoeghese.fc0.world.kingdom.Voronoi;
import tk.valoeghese.fc0.world.player.Player;
import tk.valoeghese.fc0.world.tile.Tile;
import tk.valoeghese.sod.BinaryData;
import tk.valoeghese.sod.ByteArrayDataSection;
import tk.valoeghese.sod.DataSection;
import tk.valoeghese.sod.IntArrayDataSection;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public abstract class Chunk implements World {
	public Chunk(ChunkAccess parent, int x, int z, byte[] tiles, byte[] meta, @Nullable int[] kingdoms) {
		this.parent = parent;
		this.tiles = tiles;
		this.meta = meta;
		this.blockLighting = new byte[tiles.length];
		this.skyLighting = new byte[tiles.length];
		this.nextBlockLighting = new byte[tiles.length];
		this.nextSkyLighting = new byte[tiles.length];
		this.x = x;
		this.z = z;
		this.startX = x << 4;
		this.startZ = z << 4;
		this.pos = new ChunkPos(x, z);

		for (int y = 0; y < WORLD_HEIGHT; ++y) {
			boolean check = true;

			for (int checx = 0; checx < 16; ++checx) {
				for (int checz = 0; checz < 16; ++checz) {
					Tile tile = Tile.BY_ID[this.readTile(checx, y, checz)];
					this.iota += tile.iota;

					if (check && tile.dontOptimiseOut()) {
						this.heightsToRender.add(y);
						check = false;
					}
				}
			}
		}

		long seed = parent.getSeed();

		if (kingdoms == null) {
			this.kingdoms = new int[16 * 16];

			for (int kx = 0; kx < 16; ++kx) {
				float sampleX = (this.startX + kx) / Kingdom.SCALE;

				for (int kz = 0; kz < 16; ++kz) {
					float sampleZ = (this.startZ + kz) / Kingdom.SCALE;
					this.kingdoms[kx * 16 + kz] = Voronoi.sampleVoronoi(sampleX, sampleZ, (int) seed, 0.5f).id();
				}
			}
		} else {
			this.kingdoms = kingdoms;
		}
	}

	protected byte[] tiles;
	protected byte[] meta;
	protected byte[] blockLighting;
	protected byte[] nextBlockLighting;
	protected byte[] skyLighting;
	protected byte[] nextSkyLighting;
	private final int[] kingdoms;
	private final int[] heightmap = new int[16 * 16]; // heightmap of opaque blocks for lighting calculations
	private byte skyLight = -1;
	public final int x;
	public final int z;
	private final ChunkPos pos;
	public final int startX;
	public final int startZ;
	protected final IntSet heightsToRender = new IntArraySet();
	private List<Player> players = new ArrayList<>();
	protected ChunkAccess parent;
	private float iota = 0.0f;
	public boolean populated = false;
	public ChunkLoadStatus status = ChunkLoadStatus.GENERATE;
	public boolean needsLightingCalcOnLoad = true; // false if the chunk has ever been in the TICKING stage before.
	public boolean render = false;
	// whether the chunk will have to save. Can be caused by an entity, meta, lighting, or tile change.
	// players are stored separately so don't count
	private boolean dirty = false;

	// Threading
	private static final ExecutorService lightingExecutor = Executors.newSingleThreadExecutor();

	@Override
	public GameplayWorld getGameplayWorld() {
		if (this.parent instanceof GameplayWorld) {
			return (GameplayWorld) this.parent;
		} else {
			return null;
		}
	}

	@Override
	public double sampleNoise(double x, double y) {
		return 0;
	}

	@Override
	public byte readTile(int x, int y, int z) {
		return this.tiles[index(x, y, z)];
	}

	@Override
	public byte readMeta(int x, int y, int z) {
		return this.meta[index(x, y, z)];
	}

	@Nullable
	private Chunk loadLightingChunk(int chunkX, int chunkZ) {
		return this.parent.loadChunk(chunkX, chunkZ, ChunkLoadStatus.GENERATE);
	}

	public byte getLightLevel(int x, int y, int z) {
		int i = index(x, y, z);
		return (byte) Math.max(this.blockLighting[i], this.skyLighting[i]);
	}

	public String getLightLevelText(int x, int y, int z) {
		if (isInWorld(x, y, z)) {
			int i = index(x, y, z);
			byte block = this.blockLighting[i];
			byte sky = this.skyLighting[i];
			return "Lighting: " + Math.max(this.blockLighting[i], this.skyLighting[i]) + "(block: " + block + ", sky:" + sky + ")";
		} else {
			return "Lighting: N/A (out of world)";
		}
	}

	private byte getLightLevelOverflowing(int x, int y, int z) {
		boolean isPrevChunk;

		// Check if this is out of chunk
		if ((isPrevChunk = x < 0) || x > 15) {
			Chunk c = this.loadLightingChunk(isPrevChunk ? this.x - 1 : this.x + 1, this.z);
			return c == null ? 0 : c.getLightLevel(isPrevChunk ? 15 : 0, y, z);
		} else if ((isPrevChunk = z < 0) || z > 15) {
			Chunk c = this.loadLightingChunk(this.x, isPrevChunk ? this.z - 1 : this.z + 1);
			return c == null ? 0 : c.getLightLevel(x, y, isPrevChunk ? 15 : 0);
		}

		if (y < 0 || y >= WORLD_HEIGHT) {
			return 0;
		} else {
			return this.getLightLevel(x, y, z);
		}
	}

	// This method is first called as part of first bringing a chunk to TICK
	public void updateLighting() {
		List<Chunk> chunks = new ArrayList<>();

		// Recalculate in this and all surrounding chunks which could update this chunk.
		chunks.add(this);
		chunks.add(this.loadLightingChunk(this.x - 1, this.z));
		chunks.add(this.loadLightingChunk(this.x - 1, this.z - 1));
		chunks.add(this.loadLightingChunk(this.x, this.z - 1));
		chunks.add(this.loadLightingChunk(this.x + 1, this.z - 1));
		chunks.add(this.loadLightingChunk(this.x + 1, this.z));
		chunks.add(this.loadLightingChunk(this.x + 1, this.z + 1));
		chunks.add(this.loadLightingChunk(this.x, this.z + 1));
		chunks.add(this.loadLightingChunk(this.x, this.z - 1));

		lightingExecutor.execute(() -> {
			Set<Chunk> updated = new HashSet<>();

			// Reset chunk lighting in updated chunks
			for (int i = chunks.size() - 1; i >= 0; --i) {
				Chunk c = chunks.get(i);

				if (c == null) {
					chunks.remove(i);
				} else {
					Arrays.fill(c.nextBlockLighting, (byte) 0);
					Arrays.fill(c.nextSkyLighting, (byte) 0);
				}
			}

			// Now that lighting is reset for these chunks, re-calculate it for each chunk in the list
			for (Chunk c : chunks) {
				c.calculateSkyLighting(updated);
				c.calculateBlockLighting(updated);
				c.dirty = true;
			}

			Game2fc game = Game2fc.getInstance();

			for (Chunk c : updated) {
				game.needsLightingUpdate(c);
			}
		});
	}

	public void updateSkyLighting() {
		lightingExecutor.execute(() -> {
			Set<Chunk> updated = new HashSet<>();

			// Reset chunk lighting
			Arrays.fill(this.nextSkyLighting, (byte) 0);
			this.calculateSkyLighting(updated);
			this.dirty = true;

			Game2fc game = Game2fc.getInstance();

			for (Chunk c : updated) {
				game.needsLightingUpdate(c);
			}
		});
	}

	private void calculateSkyLighting(Set<Chunk> updated) {
		if (this.skyLight == -1) {
			this.skyLight = this.parent.getGameplayWorld().getSkyLight();
		}

		for (int x = 0; x < 16; ++x) {
			for (int z = 0; z < 16; ++z) {
				int y = this.heightmap[x * 16 + z] + 1;
				this.propagateSkyLight(updated, x, y, z, this.skyLight, false, true);
			}
		}
	}

	private void calculateBlockLighting(Set<Chunk> updated) {
		int light;
		updated.add(this);

		for (int y : this.heightsToRender) {
			for (int x = 0; x < 16; ++x) {
				for (int z = 0; z < 16; ++z) {
					if ((light = Tile.BY_ID[this.readTile(x, y, z)].getLight()) > 0) {
						this.propagateBlockLight(updated, x, y, z, light, false);
					}
				}
			}
		}
	}

	public void refreshLighting() {
		System.arraycopy(this.nextBlockLighting, 0, this.blockLighting, 0, this.nextBlockLighting.length);
		System.arraycopy(this.nextSkyLighting, 0, this.skyLighting, 0, this.nextSkyLighting.length);
	}

	private boolean propagateBlockLight(Set<Chunk> updated, int x, int y, int z, int light, boolean checkOpaque) {
		boolean isPrevChunk;

		// Check if this is out of chunk
		if ((isPrevChunk = x < 0) || x > 15) {
			Chunk c = this.loadLightingChunk(isPrevChunk ? this.x - 1 : this.x + 1, this.z);

			if (c == null) {
				return false;
			} else {
				updated.add(c);
				return c.propagateBlockLight(updated, isPrevChunk ? 15 : 0, y, z, light, checkOpaque);
			}
		} else if ((isPrevChunk = z < 0) || z > 15) {
			Chunk c = this.loadLightingChunk(this.x, isPrevChunk ? this.z - 1 : this.z + 1);

			if (c == null) {
				return false;
			} else {
				updated.add(c);
				return c.propagateBlockLight(updated, x, y, isPrevChunk ? 15 : 0, light, checkOpaque);
			}
		}

		int idx = index(x, y, z);

		if (y < 0 || y >= WORLD_HEIGHT || light == 0 || (checkOpaque && Tile.BY_ID[this.tiles[idx]].isOpaqueToLight())) {
			return false;
		}

		if (this.nextBlockLighting[idx] < light) {
			this.nextBlockLighting[idx] = (byte) light;

			if (light > 1) {
				this.propagateBlockLight(updated, x - 1, y, z, light - 1, true);
				this.propagateBlockLight(updated, x + 1, y, z, light - 1, true);
				this.propagateBlockLight(updated, x, y - 1, z, light - 1, true);
				this.propagateBlockLight(updated, x, y + 1, z, light - 1, true);
				this.propagateBlockLight(updated, x, y, z - 1, light - 1, true);
				this.propagateBlockLight(updated, x, y, z + 1, light - 1, true);
			}

			return true;
		}

		return false;
	}

	@Override
	public Kingdom getKingdom(int x, int z) {
		return this.parent.kingdomById(this.kingdoms[x * 16 + z], this.startX + x, this.startZ + z);
	}

	@Override
	public int getKingdomId(int x, int z) {
		return this.kingdoms[x * 16 + z];
	}

	private boolean propagateSkyLight(Set<Chunk> updated, int x, int y, int z, int light, boolean checkOpaque, boolean slowUpDecay) {
		boolean isPrevChunk;

		// Check if this is out of chunk
		if ((isPrevChunk = x < 0) || x > 15) {
			Chunk c = this.loadLightingChunk(isPrevChunk ? this.x - 1 : this.x + 1, this.z);

			if (c == null) {
				return false;
			} else {
				updated.add(c);
				return c.propagateSkyLight(updated, isPrevChunk ? 15 : 0, y, z, light, checkOpaque, false);
			}
		} else if ((isPrevChunk = z < 0) || z > 15) {
			Chunk c = this.loadLightingChunk(this.x, isPrevChunk ? this.z - 1 : this.z + 1);

			if (c == null) {
				return false;
			} else {
				updated.add(c);
				return c.propagateSkyLight(updated, x, y, isPrevChunk ? 15 : 0, light, checkOpaque, false);
			}
		}

		int idx = index(x, y, z);

		if (y < 0 || y >= WORLD_HEIGHT || light == 0 || (checkOpaque && Tile.BY_ID[this.tiles[idx]].isOpaqueToLight())) {
			return false;
		}

		if (this.nextSkyLighting[idx] < light) {
			this.nextSkyLighting[idx] = (byte) light;

			if (light > 1) {
				this.propagateSkyLight(updated, x - 1, y, z, light - 1, true, false);
				this.propagateSkyLight(updated, x + 1, y, z, light - 1, true, false);
				this.propagateSkyLight(updated, x, y - 1, z, light - 1, true, false);
				this.propagateSkyLight(updated, x, y + 1, z, (slowUpDecay && (y & 0b1) == 1) ? light : light - 1, true, slowUpDecay);
				this.propagateSkyLight(updated, x, y, z - 1, light - 1, true, false);
				this.propagateSkyLight(updated, x, y, z + 1, light - 1, true, false);
			}

			return true;
		}

		return false;
	}

	public void computeHeightmap() {
		for (int bx = 0; bx < 16; ++bx) {
			for (int bz = 0; bz < 16; ++bz) {
				for (int by = WORLD_HEIGHT - 1; by >= 0; --by) {
					if (Tile.BY_ID[this.readTile(bx, by, bz)].isOpaqueToLight()) {
						this.heightmap[bx * 16 + bz] = by;
						break;
					}
				}
			}
		}
	}

	@Override
	public void writeTile(int x, int y, int z, byte tile) {
		int i = index(x, y, z);
		byte oldTile = this.tiles[i];

		if (tile != oldTile) {
			this.dirty = true;

			Tile oldTileO = Tile.BY_ID[this.tiles[i]];
			Tile newTileO = Tile.BY_ID[tile];
			this.iota -= oldTileO.iota;
			this.tiles[i] = tile;
			this.iota += newTileO.iota;

			if (Tile.BY_ID[tile].dontOptimiseOut()) {
				this.heightsToRender.add(y);
			} else {
				search:
				{
					for (int checx = 0; checx < 16; ++checx) {
						for (int checz = 0; checz < 16; ++checz) {
							if (Tile.BY_ID[this.readTile(checx, y, checz)].dontOptimiseOut()) {
								break search;
							}
						}
					}

					this.heightsToRender.remove(y);
				}
			}

			// Modify Heightmap

			boolean hasUpdatedLighting = false;

			if (status.isFull()) {
				int horizontalLoc = x * 16 + z;
				int height = this.heightmap[horizontalLoc];

				if (height > y) {
					if (newTileO.isOpaqueToLight()) {
						this.heightmap[horizontalLoc] = y;
						hasUpdatedLighting = true;
						this.updateLighting();
					}
				} else if (height == y) {
					if (!newTileO.isOpaqueToLight()) {
						// Recompute for y
						for (int by = WORLD_HEIGHT - 1; by >= 0; --by) {
							if (Tile.BY_ID[this.readTile(x, by, z)].isOpaqueToLight()) {
								this.heightmap[horizontalLoc] = by;
							}
						}

						hasUpdatedLighting = true;
						this.updateLighting();
					}
				}

				if (!hasUpdatedLighting) {
					if ((this.status.isFull() && (oldTileO.getLight() != newTileO.getLight()))
							|| (!newTileO.isOpaqueToLight() && shouldUpdateLight(x, y, z))) {
						this.updateLighting();
					}
				}
			}
		}
	}

	protected boolean shouldUpdateLight(int x, int y, int z) {
		return this.getLightLevelOverflowing(x, y + 1, z) > 1 || this.getLightLevelOverflowing(x, y - 1, z) > 1 ||
				this.getLightLevelOverflowing(x + 1, y, z) > 1 || this.getLightLevelOverflowing(x - 1, y, z) > 1 ||
				this.getLightLevelOverflowing(x, y, z + 1) > 1 || this.getLightLevelOverflowing(x, y, z - 1) > 1;
	}

	@Override
	public void writeMeta(int x, int y, int z, byte meta) {
		int i = index(x, y, z);

		if (this.meta[i] == meta) {
			return;
		}

		this.dirty = true;
		this.meta[i] = meta;
	}

	@Override
	public boolean isInWorld(TilePos pos) {
		return pos.isValidForChunk();
	}

	@Override
	public boolean isInWorld(int x, int y, int z) {
		return this.isInWorld(new TilePos(x, y, z));
	}

	@Override
	public int getHeight(int x, int z, Predicate<Tile> solid) {
		for (int y = 127; y >= 0; --y) {
			if (this.heightsToRender.contains(y)) {
				if (solid.test(Tile.BY_ID[this.readTile(x, y, z)])) {
					return y;
				}
			}
		}

		return 0;
	}

	void addPlayer(Player player) {
		if (!this.players.contains(player)) {
			this.players.add(player);
		}
	}

	void removePlayer(Player player) {
		if (this.players.contains(player)) {
			this.players.remove(player);
		}
	}

	@Override
	public void updateChunkOf(Player player) {
		if (player.chunk != this) {
			if (player.chunk != null) {
				player.chunk.removePlayer(player);
			}

			player.chunk = this;
			this.addPlayer(player);
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public long getSeed() {
		return this.parent.getSeed();
	}

	public ChunkPos getPos() {
		return this.pos;
	}

	public void write(BinaryData data) {
		ByteArrayDataSection tiles = new ByteArrayDataSection();
		ByteArrayDataSection lighting = new ByteArrayDataSection();
		ByteArrayDataSection lightingSky = new ByteArrayDataSection();

		for (int i = 0; i < this.tiles.length; ++i) {
			tiles.writeByte(this.tiles[i]);
			tiles.writeByte(this.meta[i]);
			lighting.writeByte(this.blockLighting[i]);
			lightingSky.writeByte(this.skyLighting[i]);
		}

		IntArrayDataSection heightmap = new IntArrayDataSection();

		for (int i : this.heightmap) {
			heightmap.writeInt(i);
		}

		IntArrayDataSection kingdoms = new IntArrayDataSection();

		for (int i : this.kingdoms) {
			kingdoms.writeInt(i);
		}

		DataSection properties = new DataSection();
		properties.writeInt(this.x);
		properties.writeInt(this.z);
		properties.writeBoolean(this.populated);
		properties.writeBoolean(this.needsLightingCalcOnLoad);
		properties.writeInt(this.skyLight);

		data.put("tiles", tiles);
		data.put("properties", properties);
		data.put("lighting", lighting);
		data.put("lightingSky", lightingSky);
		data.put("heightmap", heightmap);
		data.put("kingdoms", kingdoms);
	}

	@Nullable
	@Override
	public Chunk getChunk(int x, int z) {
		return this.parent.loadChunk(x, z, ChunkLoadStatus.POPULATE);
	}

	public boolean isDirty() {
		return this.dirty;
	}

	public void assertSkylight(byte skyLight) {
		if (this.skyLight != skyLight) {
			this.skyLight = skyLight;
			this.updateLighting(); // Since executor is single thread, should not cause problems
		}
	}

	public void assertSkylightSingle(byte skyLight) {
		if (this.skyLight != skyLight) {
			this.skyLight = skyLight;
			this.updateSkyLighting(); // Since executor is single thread, should not cause problems
		}
	}

	public void setSkylight(byte skyLight) {
		this.skyLight = skyLight;
	}

	public static <T extends Chunk> T read(ChunkAccess parent, WorldGen.ChunkConstructor<T> constructor, BinaryData data) {
		ByteArrayDataSection tileData = data.getByteArray("tiles");
		byte[] tiles = new byte[16 * 16 * WORLD_HEIGHT];
		byte[] meta = new byte[tiles.length];
		int[] kingdoms = new int[16 * 16];

		if (data.containsSection("kingdoms")) {
			IntArrayDataSection kingdomsSec = data.getIntArray("kingdoms");

			for (int i = 0; i < 256; ++i) {
				kingdoms[i] = kingdomsSec.readInt(i);
			}
		}

		for (int i = 0; i < tileData.size() / 2; ++i) {
			int j = i * 2;
			tiles[i] = tileData.readByte(j);
			meta[i] = tileData.readByte(j + 1);
		}

		DataSection properties = data.get("properties");
		T result = constructor.create(parent, properties.readInt(0), properties.readInt(1), tiles, meta, kingdoms);
		result.populated = properties.readBoolean(2);

		Chunk resultAsChunk = result;

		try {
			result.needsLightingCalcOnLoad = properties.readBoolean(3);
			resultAsChunk.skyLight = properties.readByte(4);
		} catch (Exception ignored) { // @reason support between versions
		}

		if (data.containsSection("lighting")) {
			ByteArrayDataSection lighting = data.getByteArray("lighting");

			for (int i = 0; i < lighting.size(); ++i) {
				result.blockLighting[i] = lighting.readByte(i);
			}
		}

		if (data.containsSection("lightingSky")) {
			ByteArrayDataSection lightingSky = data.getByteArray("lightingSky");

			for (int i = 0; i < lightingSky.size(); ++i) {
				result.skyLighting[i] = lightingSky.readByte(i);
			}
		}

		if (data.containsSection("heightmap")) {
			IntArrayDataSection heightmap = data.getIntArray("heightmap");

			for (int i = 0; i < heightmap.size(); ++i) {
				resultAsChunk.heightmap[i] = heightmap.readInt(i);
			}
		}

		return result;
	}

	public static void shutdown() {
		lightingExecutor.shutdownNow();

		try {
			if (!lightingExecutor.awaitTermination(300, TimeUnit.MILLISECONDS)) {
				System.out.println("Forcing Lighting Thread Shutdown");
				System.exit(0);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static int index(int x, int y, int z) {
		return (x << 11) | (z << 7) | y;
	}
}