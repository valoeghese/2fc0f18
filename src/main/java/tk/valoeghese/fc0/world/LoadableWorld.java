package tk.valoeghese.fc0.world;

import tk.valoeghese.fc0.util.maths.ChunkPos;

public interface LoadableWorld extends World {
	void chunkLoad(ChunkPos centrePos);
}
