package tk.valoeghese.fc0.client.world;

import tk.valoeghese.fc0.client.model.ChunkMesh;
import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.ChunkAccess;
import tk.valoeghese.fc0.world.RenderedChunk;
import tk.valoeghese.fc0.world.tile.Tile;

public class ClientChunk extends Chunk implements RenderedChunk {
	public ClientChunk(ChunkAccess parent, int x, int z, byte[] tiles) {
		super(parent, x, z, tiles);
	}

	protected ChunkMesh mesh;

	@Override
	public void writeTile(int x, int y, int z, byte tile) {
		int i = index(x, y, z);

		if (this.tiles[i] == tile) {
			return;
		}

		super.writeTile(x, y, z, tile);
		this.updateMesh(i, tile);

		if (x == 0) {
			ClientChunk chunk = (ClientChunk) this.parent.getChunk(this.x - 1, this.z);

			if (chunk != null) {
				chunk.rebuildMesh();
			}
		} else if (x == 15) {
			ClientChunk chunk = (ClientChunk) this.parent.getChunk(this.x + 1, this.z);

			if (chunk != null) {
				chunk.rebuildMesh();
			}
		}

		if (z == 0) {
			ClientChunk chunk = (ClientChunk) this.parent.getChunk(this.x, this.z - 1);

			if (chunk != null) {
				chunk.rebuildMesh();
			}
		} else if (z == 15) {
			ClientChunk chunk = (ClientChunk) this.parent.getChunk(this.x, this.z + 1);

			if (chunk != null) {
				chunk.rebuildMesh();
			}
		}
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
	public void destroy() {
		this.mesh.destroy();
	}
}
