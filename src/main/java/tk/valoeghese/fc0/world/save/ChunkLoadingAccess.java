package tk.valoeghese.fc0.world.save;

import tk.valoeghese.fc0.world.chunk.Chunk;
import tk.valoeghese.fc0.world.ChunkAccess;
import tk.valoeghese.fc0.world.chunk.ChunkLoadStatus;

public interface ChunkLoadingAccess<T extends Chunk> extends ChunkAccess {
	/**
	 * Places the chunk at its position.
	 * @param chunk the chunk.
	 * @param status the status of the chunk required.
	 */
	void addLoadedChunk(T chunk, ChunkLoadStatus status);
}
