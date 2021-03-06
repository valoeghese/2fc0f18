package tk.valoeghese.fc0.client.world;

import tk.valoeghese.fc0.client.render.model.ChunkMesh;
import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.ChunkAccess;
import tk.valoeghese.fc0.world.ChunkLoadStatus;
import tk.valoeghese.fc0.world.World;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nullable;

public class ClientChunk extends Chunk implements RenderedChunk {
	public ClientChunk(ChunkAccess parent, int x, int z, byte[] tiles, byte[] meta, @Nullable int[] kingdoms) {
		super(parent, x, z, tiles, meta, kingdoms);
	}

	protected ChunkMesh mesh;
	public boolean dirtyForRender = true;

	@Override
	public void writeMeta(int x, int y, int z, byte meta) {
		int i = index(x, y, z);

		if (this.meta[i] == meta) {
			return;
		}

		this.dirtyForRender = true;
		super.writeMeta(x, y, z, meta);
	}

	@Override
	public void writeTile(int x, int y, int z, byte tile) {
		int i = index(x, y, z);

		if (this.tiles[i] == tile) {
			return;
		}

		this.dirtyForRender = true;
		super.writeTile(x, y, z, tile);

		if (x == 0) {
			ClientChunk chunk = (ClientChunk) this.getRenderChunk(this.x - 1, this.z);

			if (chunk != null) {
				chunk.dirtyForRender = true;
			}
		} else if (x == 15) {
			ClientChunk chunk = (ClientChunk) this.getRenderChunk(this.x + 1, this.z);

			if (chunk != null) {
				chunk.dirtyForRender = true;
			}
		}

		if (z == 0) {
			ClientChunk chunk = (ClientChunk) this.getRenderChunk(this.x, this.z - 1);

			if (chunk != null) {
				chunk.dirtyForRender = true;
			}
		} else if (z == 15) {
			ClientChunk chunk = (ClientChunk) this.getRenderChunk(this.x, this.z + 1);

			if (chunk != null) {
				chunk.dirtyForRender = true;
			}
		}
	}

	@Override
	public boolean renderHeight(int y) {
		return (y >= 0 && y < WORLD_HEIGHT) ? this.heightsToRender.contains(y) : false;
	}

	@Override
	public Tile north(int x, int y) {
		Chunk chunk = this.getChunk(this.x, this.z + 1);

		if (chunk == null) {
			return Tile.AIR;
		} else {
			return Tile.BY_ID[chunk.readTile(x, y, 0)];
		}
	}

	@Override
	public Tile south(int x, int y) {
		Chunk chunk = this.getChunk(this.x, this.z - 1);

		if (chunk == null) {
			return Tile.AIR;
		} else {
			return Tile.BY_ID[chunk.readTile(x, y, 15)];
		}
	}

	@Override
	public Tile east(int z, int y) {
		Chunk chunk = this.getChunk(this.x + 1, this.z);

		if (chunk == null) {
			return Tile.AIR;
		} else {
			return Tile.BY_ID[chunk.readTile(0, y, z)];
		}
	}

	@Override
	public Tile west(int z, int y) {
		Chunk chunk = this.getChunk(this.x - 1, this.z);

		if (chunk == null) {
			return Tile.AIR;
		} else {
			return Tile.BY_ID[chunk.readTile(15, y, z)];
		}
	}

	void rebuildMesh() {
		if (this.mesh != null) {
			this.mesh.buildMesh(this.tiles, this.meta);
		}
	}

	private void lebronJames() {
		if (this.dirtyForRender && this.mesh != null) {
			this.dirtyForRender = false;
			this.mesh.buildMesh(this.tiles, this.meta);
		}
	}

	public ChunkMesh getOrCreateMesh() {
		if (this.mesh == null) {
			this.mesh = new ChunkMesh(this, this.x, this.z);
		}

		this.lebronJames();

		return this.mesh;
	}

	@Nullable
	public Chunk getRenderChunk(int x, int z) {
		return this.parent.getRenderChunk(x, z);
	}

	@Override
	public void destroy() {
		if (this.mesh != null) {
			this.mesh.destroy();
		}
	}

	@Override
	public void refreshLighting() {
		super.refreshLighting();

		if (this.status == ChunkLoadStatus.RENDER) { // Is this necessary?
			this.dirtyForRender = true;
		}
	}

	@Override
	public float getRenderLightingFactor(int x, int y, int z) {
		if (y < 0 || y > World.WORLD_HEIGHT) {
			return 0.1f;
		}

		boolean isPrevChunk;

		// Check if this is out of chunk
		if ((isPrevChunk = x < 0) || x > 15) {
			Chunk c = this.getChunk(isPrevChunk ? this.x - 1 : this.x + 1, this.z);

			if (c == null) {
				return 0.1f;
			}
			return renderLighting(c.getLightLevel(isPrevChunk ? 15 : 0, y, z));
		} else if ((isPrevChunk = z < 0) || z > 15) {
			Chunk c = this.getChunk(this.x, isPrevChunk ? this.z - 1 : this.z + 1);

			if (c == null) {
				return 0.1f;
			}
			return renderLighting(c.getLightLevel(x, y, isPrevChunk ? 15 : 0));
		}

		return renderLighting(this.getLightLevel(x, y, z));
	}

	// Maps from [0,15] to approximately [root(0.1),1], then squares it
	// https://www.desmos.com/calculator/xmmyzvljzt
	private static float renderLighting(int level) {
		float base = (0.045584f * level + 0.316228f);
		return base * base;
	}
}
