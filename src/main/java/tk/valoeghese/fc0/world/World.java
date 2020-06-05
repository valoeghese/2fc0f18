package tk.valoeghese.fc0.world;

import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.gen.GenWorld;
import tk.valoeghese.fc0.world.player.Player;
import tk.valoeghese.fc0.world.tile.Tile;

public interface World extends GenWorld {
	default byte readTile(TilePos pos) {
		return this.readTile(pos.x, pos.y, pos.z);
	}

	default void writeTile(TilePos pos, byte tile) {
		this.writeTile(pos.x, pos.y, pos.z, tile);
	}

	default int getHeight(int x, int z) {
		return this.getHeight(x, z, Tile::shouldRender);
	}

	default boolean isInWorld(TilePos pos) {
		return this.isInWorld(pos.x, pos.y, pos.z);
	}

	default byte readMeta(TilePos pos) {
		return this.readTile(pos.x, pos.y, pos.z);
	}

	void writeTile(int x, int y, int z, byte tile);
	void destroy();

	default void updateChunkOf(Player player) {
	}

	int WORLD_HEIGHT = 128;
}
