package tk.valoeghese.fc0.world;

import tk.valoeghese.fc0.client.ClientPlayer;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.function.Predicate;

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

	default int getHeight(int x, int z) {
		return this.getHeight(x, z, Tile::shouldRender);
	}

	byte readTile(int x, int y, int z);
	void writeTile(int x, int y, int z, byte tile);
	boolean isInWorld(int x, int y, int z);
	int getHeight(int x, int z, Predicate<Tile> solid);
	void destroy();

	default void updateChunkOf(ClientPlayer clientPlayer) {
	}

	static int WORLD_HEIGHT = 128;
}
