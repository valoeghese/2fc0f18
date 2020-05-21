package tk.valoeghese.fc0.world;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import tk.valoeghese.fc0.client.ClientPlayer;
import tk.valoeghese.fc0.client.model.ChunkMesh;
import tk.valoeghese.fc0.util.TilePos;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Chunk implements World {
	public Chunk(int x, int z, byte[] tiles) {
		this.tiles = tiles;
		this.x = x;
		this.z = z;

		search: for (int y = 0; y < 128; ++y) {
			for (int checx = 0; checx < 16; ++checx) {
				for (int checz = 0; checz < 16; ++checz) {
					if (Tile.BY_ID[this.readTile(checx, y, checz)].shouldOptimiseOut()) {
						this.heightsToRender.add(y);
						continue search;
					}
				}
			}
		}
	}

	private byte[] tiles;
	public final int x;
	public final int z;
	private final IntSet heightsToRender = new IntArraySet();
	private ChunkMesh mesh;
	private List<ClientPlayer> players = new ArrayList<>();

	@Override
	public byte readTile(int x, int y, int z) {
		return this.tiles[index(x, y, z)];
	}

	@Override
	public void writeTile(int x, int y, int z, byte tile) {
		int i = index(x, y, z);

		if (this.tiles[i] == tile) {
			return;
		}

		this.tiles[i] = tile;

		if (Tile.BY_ID[tile].shouldOptimiseOut()) {
			this.heightsToRender.add(y);
		} else {
			search: {
				for (int checx = 0; checx < 16; ++checx) {
					for (int checz = 0; checz < 16; ++checz) {
						if (Tile.BY_ID[this.readTile(checx, y, checz)].shouldOptimiseOut()) {
							break search;
						}
					}
				}

				this.heightsToRender.remove(y);
			}
		}

		if (this.mesh != null) {
			this.mesh.updateTile(i, tile);
		}
	}

	public ChunkMesh getOrCreateMesh() {
		if (this.mesh == null) {
			this.mesh = new ChunkMesh(this, this.tiles, this.x, this.z);
		}

		return this.mesh;
	}

	public boolean renderHeight(int y) {
		return (y >= 0 && y < 128) ? this.heightsToRender.contains(y) : false;
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

	void addPlayer(ClientPlayer player) {
		if (!this.players.contains(player)) {
			this.players.add(player);
		}
	}

	void removePlayer(ClientPlayer player) {
		if (this.players.contains(player)) {
			this.players.remove(player);
		}
	}

	@Override
	public void updateChunkOf(ClientPlayer clientPlayer) {
		if (clientPlayer.chunk != this) {
			if (clientPlayer.chunk != null) {
				clientPlayer.chunk.removePlayer(clientPlayer);
			}

			clientPlayer.chunk = this;
			this.addPlayer(clientPlayer);
		}
	}

	static int index(int x, int y, int z) {
		return (x << 11) | (z << 7) | y;
	}
}
