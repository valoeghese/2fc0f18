package tk.valoeghese.fc0.world;

import tk.valoeghese.fc0.util.maths.ChunkPos;
import tk.valoeghese.fc0.world.chunk.Chunk;

import java.util.function.Consumer;

public interface LoadableWorld extends TileAccess {
	void chunkLoad(ChunkPos centrePos);
	ChunkPos getSpawnPos();
	void scheduleForChunk(long chunkPos, Consumer<Chunk> callback, String taskName);
}
