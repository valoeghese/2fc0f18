package tk.valoeghese.fc0.world;

import tk.valoeghese.fc0.util.TilePos;

public interface World {
	default byte readTile(TilePos pos) {
		return this.readTile(pos.x, pos.y, pos.z);
	}

	byte readTile(int x, int y, int z);
	void writeTile(int x, int y, int z, byte tile);
	boolean isInWorld(TilePos pos);
}
