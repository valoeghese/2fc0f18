package tk.valoeghese.fc0.world;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import tk.valoeghese.fc0.util.maths.ChunkPos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.gen.WorldGen;
import tk.valoeghese.fc0.world.player.Player;
import tk.valoeghese.fc0.world.tile.Tile;
import tk.valoeghese.sod.BinaryData;
import tk.valoeghese.sod.ByteArrayDataSection;
import tk.valoeghese.sod.DataSection;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public abstract class Chunk implements World {
	public Chunk(ChunkAccess parent, int x, int z, byte[] tiles, byte[] meta) {
		this.parent = parent;
		this.tiles = tiles;
		this.meta = meta;
		this.lighting = new byte[tiles.length];
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
	}

	protected byte[] tiles;
	protected byte[] meta;
	protected byte[] lighting;
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

	// This method is first called as part of first bringing a chunk to TICK
	public void updateLighting(List<Chunk> chunks) {
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

		// Reset chunk lighting in updated chunks
		for (int i = chunks.size() - 1; i >= 0; --i) {
			Chunk c = chunks.get(i);

			if (c == null) {
				chunks.remove(i);
			} else {
				Arrays.fill(c.lighting, (byte) 0);
			}
		}

		// Now that lighting is reset for these chunks, re-calculate it for each chunk in the list
		for (Chunk c : chunks) {
			c.calculateLighting();
		}
	}

	private void calculateLighting() {
		int light;

		for (int y : this.heightsToRender) {
			for (int x = 0; x < 16; ++x) {
				for (int z = 0; z < 16; ++z) {
					if ((light = Tile.BY_ID[this.readTile(x, y, z)].getLight()) > 0) {
						if (this.propagateLight(x, y, z, light, false)) {
							this.dirty = true; // Save new lighting.
						}
					}
				}
			}
		}
	}

	private boolean propagateLight(int x, int y, int z, int light, boolean checkOpaque) {
		boolean isPrevChunk;

		// Check if this is out of chunk
		if ((isPrevChunk = x < 0) || x > 15) {
			return this.loadLightingChunk(isPrevChunk ? this.x - 1 : this.x + 1, this.z).propagateLight(isPrevChunk ? 15 : 0, y, z, light, checkOpaque);
		} else if ((isPrevChunk = z < 0) || z > 15) {
			return this.loadLightingChunk(this.x, isPrevChunk ? this.z - 1 : this.z + 1).propagateLight(x, y, isPrevChunk ? 15 : 0, light, checkOpaque);
		}

		int idx = index(x, y, z);

		if (y < 0 || y > WORLD_HEIGHT || light == 0 || (checkOpaque && Tile.BY_ID[this.tiles[idx]].isOpaque())) {
			return false;
		}

		if (this.lighting[idx] < light) {
			this.lighting[idx] = (byte) light;

			if (light > 1) {
				this.propagateLight(x - 1, y, z, light - 1, true);
				this.propagateLight(x + 1, y, z, light - 1, true);
				this.propagateLight(x, y - 1, z, light - 1, true);
				this.propagateLight(x, y + 1, z, light - 1, true);
				this.propagateLight(x, y, z - 1, light - 1, true);
				this.propagateLight(x, y, z + 1, light - 1, true);
			}

			return true;
		}

		return false;
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

			if (this.status.isFull() && (oldTileO.getLight() != newTileO.getLight())) {
				System.out.println("Updating lighting");
				this.updateLighting(new ArrayList<>());
			}
		}
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

		DataSection properties = new DataSection();
		properties.writeInt(this.x);
		properties.writeInt(this.z);
		properties.writeBoolean(this.populated);
		properties.writeBoolean(this.needsLightingCalcOnLoad);

		data.put("tiles", tiles);
		data.put("properties", properties);
		data.put("lighting", lighting);
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

		try {
			result.needsLightingCalcOnLoad = properties.readBoolean(3);
		} catch (Exception ignored) { // @reason support between versions
		}

		if (data.containsSection("lighting")) {
			ByteArrayDataSection lighting = data.getByteArray("lighting");

			for (int i = 0; i < lighting.size(); ++i) {
				result.lighting[i] = lighting.readByte(i);
			}
		}

		return result;
	}

	public static int index(int x, int y, int z) {
		return (x << 11) | (z << 7) | y;
	}
}
