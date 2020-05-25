package tk.valoeghese.fc0.world;

import javax.annotation.Nullable;

public interface ChunkAccess {
	@Nullable
	Chunk getChunk(int x, int z);
	long getSeed();
}
