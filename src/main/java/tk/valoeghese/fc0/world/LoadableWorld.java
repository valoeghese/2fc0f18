package tk.valoeghese.fc0.world;

import tk.valoeghese.fc0.util.maths.ChunkPos;

public interface LoadableWorld extends TileAccess {
	void chunkLoad(ChunkPos centrePos);
	ChunkPos getSpawnPos();
}
