package tk.valoeghese.fc0.world;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import tk.valoeghese.fc0.client.ClientPlayer;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class Chunk implements World {
	public Chunk(ChunkAccess parent, int x, int z, byte[] tiles) {
		this.parent = parent;
		this.tiles = tiles;
		this.x = x;
		this.z = z;
		this.startX = x << 4;
		this.startZ = z << 4;

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
	public final int x;
	public final int z;
	public final int startX;
	public final int startZ;
	protected final IntSet heightsToRender = new IntArraySet();
	private List<Player> players = new ArrayList<>();
	protected ChunkAccess parent;
	private float iota = 0.0f;

	@Override
	public byte readTile(int x, int y, int z) {
		return this.tiles[index(x, y, z)];
	}

	@Override
	public void writeTile(int x, int y, int z, byte tile) {
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

	public static int index(int x, int y, int z) {
		return (x << 11) | (z << 7) | y;
	}
}
