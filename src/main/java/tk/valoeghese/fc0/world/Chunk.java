package tk.valoeghese.fc0.world;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.util.maths.ChunkPos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.gen.WorldGen;
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
	public Chunk(ChunkAccess parent, int x, int z, byte[] tiles, byte[] meta) {
		this.parent = parent;
		this.tiles = tiles;
		this.meta = meta;
		this.lighting = new byte[tiles.length];
		this.nextLighting = new byte[tiles.length];
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

		this.computeHeightmap();
	}

	protected byte[] tiles;
	protected byte[] meta;
	protected byte[] lighting;
	protected byte[] nextLighting;
	private final int[] heightmap = new int[16 * 16];
	private byte skyLight = 0;
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
		return this.lighting[index(x, y, z)];
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
					Arrays.fill(c.nextLighting, (byte) 0);
				}
			}

			// Now that lighting is reset for these chunks, re-calculate it for each chunk in the list
			for (Chunk c : chunks) {
				c.calculateLighting(updated);
				c.dirty = true;
			}

			Client2fc.getInstance().runLater(() -> {
				for (Chunk c : updated) {
					c.refreshLighting();
				}
			});
		});
	}

	private void calculateLighting(Set<Chunk> updated) {
		int light;
		updated.add(this);

		for (int y : this.heightsToRender) {
			for (int x = 0; x < 16; ++x) {
				for (int z = 0; z < 16; ++z) {
					if ((light = Tile.BY_ID[this.readTile(x, y, z)].getLight()) > 0) {
						this.propagateLight(updated, x, y, z, light, false);
					}
				}
			}
		}
	}

	protected void refreshLighting() {
		System.arraycopy(this.nextLighting, 0, this.lighting, 0, this.nextLighting.length);
	}

	private boolean propagateLight(Set<Chunk> updated, int x, int y, int z, int light, boolean checkOpaque) {
		boolean isPrevChunk;

		// Check if this is out of chunk
		if ((isPrevChunk = x < 0) || x > 15) {
			Chunk c = this.loadLightingChunk(isPrevChunk ? this.x - 1 : this.x + 1, this.z);

			if (c == null) {
				return false;
			} else {
				updated.add(c);
				return c.propagateLight(updated, isPrevChunk ? 15 : 0, y, z, light, checkOpaque);
			}
		} else if ((isPrevChunk = z < 0) || z > 15) {
			Chunk c = this.loadLightingChunk(this.x, isPrevChunk ? this.z - 1 : this.z + 1);

			if (c == null) {
				return false;
			} else {
				updated.add(c);
				return c.propagateLight(updated, x, y, isPrevChunk ? 15 : 0, light, checkOpaque);
			}
		}

		int idx = index(x, y, z);

		if (y < 0 || y >= WORLD_HEIGHT || light == 0 || (checkOpaque && Tile.BY_ID[this.tiles[idx]].isOpaque())) {
			return false;
		}

		if (this.nextLighting[idx] < light) {
			this.nextLighting[idx] = (byte) light;

			if (light > 1) {
				this.propagateLight(updated, x - 1, y, z, light - 1, true);
				this.propagateLight(updated, x + 1, y, z, light - 1, true);
				this.propagateLight(updated, x, y - 1, z, light - 1, true);
				this.propagateLight(updated, x, y + 1, z, light - 1, true);
				this.propagateLight(updated, x, y, z - 1, light - 1, true);
				this.propagateLight(updated, x, y, z + 1, light - 1, true);
			}

			return true;
		}

		return false;
	}

	public void computeHeightmap() {
		for (int bx = 0; bx < 16; ++bx) {
			for (int bz = 0; bz < 16; ++bz) {
				for (int by = WORLD_HEIGHT - 1; by >= 0; --by) {
					if (Tile.BY_ID[this.readTile(bx, by, bz)].shouldRender()) {
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

			int horizontalLoc = x * 16 + z;
			int height = this.heightmap[horizontalLoc];

			if (height > y) {
				if (newTileO.shouldRender()) {
					this.heightmap[horizontalLoc] = y;
				}
			} else if (height == y){
				if (!newTileO.shouldRender()) {
					// Recompute for y
					for (int by = WORLD_HEIGHT - 1; by >= 0; --by) {
						if (Tile.BY_ID[this.readTile(x, by, z)].shouldRender()) {
							this.heightmap[horizontalLoc] = by;
						}
					}
				}
			}

			if ((this.status.isFull() && (oldTileO.getLight() != newTileO.getLight()))
					|| (!newTileO.isOpaque() && shouldUpdateLight(x, y, z))) {
				this.updateLighting();
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
		this.dirty = true;
		this.meta[index(x, y, z)] = meta;
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

	public ChunkPos getPos() {
		return this.pos;
	}

	public void write(BinaryData data) {
		ByteArrayDataSection tiles = new ByteArrayDataSection();
		ByteArrayDataSection lighting = new ByteArrayDataSection();

		for (int i = 0; i < this.tiles.length; ++i) {
			tiles.writeByte(this.tiles[i]);
			tiles.writeByte(this.meta[i]);
			lighting.writeByte(this.lighting[i]);
		}

		IntArrayDataSection heightmap = new IntArrayDataSection();

		for (int i : this.heightmap) {
			heightmap.writeInt(i);
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
		data.put("heightmap", heightmap);
	}

	@Nullable
	@Override
	public Chunk getChunk(int x, int z) {
		return this.parent.loadChunk(x, z, ChunkLoadStatus.POPULATE);
	}

	public boolean isDirty() {
		return this.dirty;
	}

	public static <T extends Chunk> T read(ChunkAccess parent, WorldGen.ChunkConstructor<T> constructor, BinaryData data) {
		ByteArrayDataSection tileData = data.getByteArray("tiles");
		byte[] tiles = new byte[16 * 16 * WORLD_HEIGHT];
		byte[] meta = new byte[tiles.length];

		for (int i = 0; i < tileData.size() / 2; ++i) {
			int j = i * 2;
			tiles[i] = tileData.readByte(j);
			meta[i] = tileData.readByte(j + 1);
		}

		DataSection properties = data.get("properties");
		T result = constructor.create(parent, properties.readInt(0), properties.readInt(1), tiles, meta);
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
				result.lighting[i] = lighting.readByte(i);
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
			if (!lightingExecutor.awaitTermination(100, TimeUnit.MICROSECONDS)) {
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
