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
import java.util.List;
import java.util.function.Predicate;

public abstract class Chunk implements World {
	public Chunk(ChunkAccess parent, int x, int z, byte[] tiles, byte[] meta) {
		this.parent = parent;
		this.tiles = tiles;
		this.meta = meta;
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
	public boolean render = false;
	// whether the chunk will have to save. Can be caused by an entity, meta, or tile change.
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

	@Override
	public void writeTile(int x, int y, int z, byte tile) {
		this.dirty = true;
		int i = index(x, y, z);

		this.iota -= Tile.BY_ID[this.tiles[i]].iota;
		this.tiles[i] = tile;
		this.iota += Tile.BY_ID[tile].iota;

		if (Tile.BY_ID[tile].dontOptimiseOut()) {
			this.heightsToRender.add(y);
		} else {
			search: {
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

		for (int i = 0; i < this.tiles.length; ++i) {
			tiles.writeByte(this.tiles[i]);
			tiles.writeByte(this.meta[i]);
		}

		DataSection properties = new DataSection();
		properties.writeInt(this.x);
		properties.writeInt(this.z);
		properties.writeBoolean(this.populated);

		data.put("tiles", tiles);
		data.put("properties", properties);
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
		return result;
	}

	public static int index(int x, int y, int z) {
		return (x << 11) | (z << 7) | y;
	}
}
