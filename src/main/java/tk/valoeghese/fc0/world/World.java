package tk.valoeghese.fc0.world;

import tk.valoeghese.fc0.util.TilePos;

public interface World {
	default byte readTile(TilePos pos) {
		return this.readTile(pos.x, pos.y, pos.z);
	}

	default void writeTile(TilePos pos, byte tile) {
		this.writeTile(pos.x, pos.y, pos.z, tile);
	}

	default boolean isInWorld(TilePos pos) {
		return this.isInWorld(pos.x, pos.y, pos.z);
	}

	byte readTile(int x, int y, int z);
	void writeTile(int x, int y, int z, byte tile);
	boolean isInWorld(int x, int y, int z);
	int getHeight(int x, int z);
}
