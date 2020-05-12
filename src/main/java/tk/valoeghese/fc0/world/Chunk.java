package tk.valoeghese.fc0.world;

import tk.valoeghese.fc0.client.model.ChunkMesh;

public class Chunk implements TileAccess {
	public Chunk(int x, int z, byte[] tiles) {
		this.tiles = tiles;
		this.x = x;
		this.z = z;
	}

	private byte[] tiles;
	public final int x;
	public final int z;
	private ChunkMesh mesh;

	@Override
	public byte readTile(int x, int y, int z) {
		return this.tiles[index(x, y, z)];
	}

	@Override
	public void writeTile(int x, int y, int z, byte tile) {
		int i = index(x, y, z);
		this.tiles[i] = tile;

		if (this.mesh != null) {
			this.mesh.updateTile(i, tile);
		}
	}

	public ChunkMesh getOrCreateMesh() {
		if (this.mesh == null) {
			this.mesh = new ChunkMesh(this.tiles, this.x, this.z);
		}

		return this.mesh;
	}

	static int index(int x, int y, int z) {
		return (x << 11) | (z << 7) | y;
	}
}
