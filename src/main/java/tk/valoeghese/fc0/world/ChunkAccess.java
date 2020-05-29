package tk.valoeghese.fc0.world;

import javax.annotation.Nullable;

public interface ChunkAccess {
	@Nullable
	Chunk loadChunk(int x, int z, ChunkLoadStatus status);
	/**
	 * Gets the chunk at RENDER stage, if it exists.
	 */
	@Nullable
	Chunk getRenderChunk(int x, int z);
	long getSeed();
}
