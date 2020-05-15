package tk.valoeghese.fc0.world;

import tk.valoeghese.fc0.util.TilePos;

public interface World {
	default byte readTile(TilePos pos) {
		return this.readTile(pos.x, pos.y, pos.z);
	}

	default void writeTile(TilePos pos, byte tile) {
		this.writeTile(pos.x, pos.y, pos.z, tile);
	}

	byte readTile(int x, int y, int z);
	void writeTile(int x, int y, int z, byte tile);
	boolean isInWorld(TilePos pos);
}
