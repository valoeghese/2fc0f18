package tk.valoeghese.fc0.client.world;

import tk.valoeghese.fc0.client.render.model.ChunkMesh;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.TileAccess;
import tk.valoeghese.fc0.world.chunk.Chunk;
import tk.valoeghese.fc0.world.chunk.ChunkLoadStatus;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nullable;

public class ClientChunk extends Chunk {
	public ClientChunk(GameplayWorld<ClientChunk> parent, int x, int z, byte[] tiles, byte[] meta, @Nullable int[] kingdoms) {
		super(parent, x, z, tiles, meta, kingdoms);
	}

	protected ChunkMesh mesh;
	public boolean dirtyForRender = true;
	public long lastMeshBuild = System.currentTimeMillis() - 10000; // just in case tm
	private int neighbourUpdates = 0;

	public boolean render = false;
	public boolean preRender = false;

	/**
	 * @param flag the flag for the neighbour, which depends on the direction from this chunk to that.
	 * @return whether all neighbours are loaded
	 */
	boolean markNeighbourLoaded(int flag) {
		this.neighbourUpdates |= flag;
		return this.neighbourUpdates >= MAX_UPDATES;
	}

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
			ClientChunk chunk = ((ClientWorld) this.parent).getRenderChunk(this.x - 1, this.z);

			if (chunk != null) {
				chunk.dirtyForRender = true;
			}
		} else if (x == 15) {
			ClientChunk chunk = ((ClientWorld) this.parent).getRenderChunk(this.x + 1, this.z);

			if (chunk != null) {
				chunk.dirtyForRender = true;
			}
		}

		if (z == 0) {
			ClientChunk chunk = ((ClientWorld) this.parent).getRenderChunk(this.x, this.z - 1);

			if (chunk != null) {
				chunk.dirtyForRender = true;
			}
		} else if (z == 15) {
			ClientChunk chunk = ((ClientWorld) this.parent).getRenderChunk(this.x, this.z + 1);

			if (chunk != null) {
				chunk.dirtyForRender = true;
			}
		}
	}

	public boolean renderHeight(int y) {
		return y >= 0 && y < WORLD_HEIGHT && this.heightsToRender.contains(y);
	}

	public Tile west(int x, int y) {
		Chunk chunk = this.getGameplayWorld().getChunk(this.x, this.z + 1);

		if (chunk == null) {
			return Tile.AIR;
		} else {
			return Tile.BY_ID[chunk.readTile(x, y, 0)];
		}
	}

	// sun rises in -z I checked.
	public Tile east(int x, int y) {
		Chunk chunk = this.getGameplayWorld().getChunk(this.x, this.z - 1);

		if (chunk == null) {
			return Tile.AIR;
		} else {
			return Tile.BY_ID[chunk.readTile(x, y, 15)];
		}
	}

	public Tile south(int z, int y) {
		Chunk chunk = this.getGameplayWorld().getChunk(this.x + 1, this.z);

		if (chunk == null) {
			return Tile.AIR;
		} else {
			return Tile.BY_ID[chunk.readTile(0, y, z)];
		}
	}

	public Tile north(int z, int y) {
		Chunk chunk = this.getGameplayWorld().getChunk(this.x - 1, this.z);

		if (chunk == null) {
			return Tile.AIR;
		} else {
			return Tile.BY_ID[chunk.readTile(15, y, z)];
		}
	}

	void rebuildMesh() {
		if (this.mesh != null) {
			this.mesh.buildMesh(this.tiles, this.meta);
			this.lastMeshBuild = System.currentTimeMillis();
		}
	}

	private void undirty() { // todo this isn't to do with the dirty boolean, but rather rendering, so maybe rename
		if (this.dirtyForRender && this.mesh != null) {
			this.dirtyForRender = false;
			this.mesh.buildMesh(this.tiles, this.meta);
			this.lastMeshBuild = System.currentTimeMillis();
		}
	}

	public ChunkMesh getOrCreateMesh() {
		if (this.mesh == null) {
			this.mesh = new ChunkMesh(this, this.x, this.z);
		}

		this.undirty();

		return this.mesh;
	}

	@Override
	public void destroy() {
		if (this.mesh != null) {
			this.mesh.destroy();
		}
	}

	@Override
	public void refreshLightingMesh() {
		if (this.status == ChunkLoadStatus.RENDER) { // Is this necessary?
			this.dirtyForRender = true; // TODO Use shadaers for lighting instead?????
		}
	}

	public int getPackedLightLevel(int x, int y, int z) {
		if (y < 0 || y > TileAccess.WORLD_HEIGHT) {
			return 0;
		}

		boolean isPrevChunk;

		// Check if this is out of chunk
		if ((isPrevChunk = x < 0) || x > 15) {
			ClientChunk c = (ClientChunk) this.getGameplayWorld().getChunk(isPrevChunk ? this.x - 1 : this.x + 1, this.z);

			if (c == null) {
				return 0;
			}
			return c.getPackedLightLevel(isPrevChunk ? 15 : 0, y, z);
		} else if ((isPrevChunk = z < 0) || z > 15) {
			ClientChunk c = (ClientChunk) this.getGameplayWorld().getChunk(this.x, isPrevChunk ? this.z - 1 : this.z + 1);

			if (c == null) {
				return 0;
			}
			return c.getPackedLightLevel(x, y, isPrevChunk ? 15 : 0);
		}

		int i = index(x, y, z);
		int result = (int)this.blockLighting[i] << 4;
		result = (result | this.skyLighting[i]) << 3;
		return result;
	}

	// Maps from [0,15] to approximately [root(0.1),1], then squares it
	// https://www.desmos.com/calculator/xmmyzvljzt
	private static float renderLighting(int level) {
		float base = (0.045584f * level + 0.316228f);
		return base * base;
	}

	private static final int MAX_UPDATES = 0b1111;
}
