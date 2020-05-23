package tk.valoeghese.fc0.world;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import tk.valoeghese.fc0.client.ClientPlayer;
import tk.valoeghese.fc0.client.model.ChunkMesh;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Chunk implements World, RenderedChunk {
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

	private byte[] tiles;
	public final int x;
	public final int z;
	public final int startX;
	public final int startZ;
	private final IntSet heightsToRender = new IntArraySet();
	private ChunkMesh mesh;
	private List<ClientPlayer> players = new ArrayList<>();
	private ChunkAccess parent;
	private float iota = 0.0f;

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

		this.updateMesh(i, tile);

		if (x == 0) {
			Chunk chunk = this.parent.getChunk(this.x - 1, this.z);

			if (chunk != null) {
				chunk.rebuildMesh();
			}
		} else if (x == 15) {
			Chunk chunk = this.parent.getChunk(this.x + 1, this.z);

			if (chunk != null) {
				chunk.rebuildMesh();
			}
		}

		if (z == 0) {
			Chunk chunk = this.parent.getChunk(this.x, this.z - 1);

			if (chunk != null) {
				chunk.rebuildMesh();
			}
		} else if (z == 15) {
			Chunk chunk = this.parent.getChunk(this.x, this.z + 1);

			if (chunk != null) {
				chunk.rebuildMesh();
			}
		}
	}

	private void rebuildMesh() {
		if (this.mesh != null) {
			this.mesh.buildMesh();
		}
	}

	private void updateMesh(int index, byte tile) {
		if (this.mesh != null) {
			this.mesh.updateTile(index, tile);
		}
	}

	public ChunkMesh getOrCreateMesh() {
		if (this.mesh == null) {
			this.mesh = new ChunkMesh(this, this.tiles, this.x, this.z);
		}

		return this.mesh;
	}

	@Override
	public boolean renderHeight(int y) {
		return (y >= 0 && y < WORLD_HEIGHT) ? this.heightsToRender.contains(y) : false;
	}

	@Override
	public Tile north(int x, int y) {
		Chunk chunk = this.parent.getChunk(this.x, this.z + 1);

		if (chunk == null) {
			return Tile.AIR;
		} else {
			return Tile.BY_ID[chunk.readTile(x, y, 0)];
		}
	}

	@Override
	public Tile south(int x, int y) {
		Chunk chunk = this.parent.getChunk(this.x, this.z - 1);

		if (chunk == null) {
			return Tile.AIR;
		} else {
			return Tile.BY_ID[chunk.readTile(x, y, 15)];
		}
	}

	@Override
	public Tile east(int z, int y) {
		Chunk chunk = this.parent.getChunk(this.x + 1, this.z);

		if (chunk == null) {
			return Tile.AIR;
		} else {
			return Tile.BY_ID[chunk.readTile(0, y, z)];
		}
	}

	@Override
	public Tile west(int z, int y) {
		Chunk chunk = this.parent.getChunk(this.x - 1, this.z);

		if (chunk == null) {
			return Tile.AIR;
		} else {
			return Tile.BY_ID[chunk.readTile(15, y, z)];
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

	@Override
	public void destroy() {
		this.mesh.destroy();
	}

	public static int index(int x, int y, int z) {
		return (x << 11) | (z << 7) | y;
	}
}
