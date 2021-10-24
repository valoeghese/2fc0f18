package tk.valoeghese.fc0.world;

import tk.valoeghese.fc0.world.chunk.Chunk;
import tk.valoeghese.fc0.world.chunk.TileWriter;
import tk.valoeghese.fc0.world.chunk.ChunkLoadStatus;
import tk.valoeghese.fc0.world.gen.SeedWorld;
import tk.valoeghese.fc0.world.kingdom.Kingdom;

import javax.annotation.Nullable;

public interface ChunkAccess extends SeedWorld {
	@Nullable
	boolean loadChunk(int x, int z, ChunkLoadStatus status);
	/**
	 * Gets the chunk at RENDER stage, if it exists.
	 */
	@Nullable
	Chunk getRenderChunk(int x, int z);
	/**
	 * Gets the chunk at TICK stage, if it exists.
	 */
	@Nullable
	Chunk getFullChunk(int x, int z);

	/**
	 * Gets a means of access for chunks at the given location. Unloaded chunks will be loaded and have the modifications retroactively applied.
	 */
	TileWriter getDelayedLoadChunk(int x, int z);

	Kingdom kingdomById(int kingdom, int x, int z);
}
