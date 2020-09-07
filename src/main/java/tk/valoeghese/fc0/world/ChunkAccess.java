package tk.valoeghese.fc0.world;

import tk.valoeghese.fc0.world.gen.SeedWorld;
import tk.valoeghese.fc0.world.gen.kingdom.Kingdom;

import javax.annotation.Nullable;

public interface ChunkAccess extends SeedWorld {
	@Nullable
	Chunk loadChunk(int x, int z, ChunkLoadStatus status);
	/**
	 * Gets the chunk at RENDER stage, if it exists.
	 */
	@Nullable
	Chunk getRenderChunk(int x, int z);

	Kingdom kingdomById(int kingdom, int x, int z);
}
