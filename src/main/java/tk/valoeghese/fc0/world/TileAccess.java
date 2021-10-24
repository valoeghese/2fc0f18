package tk.valoeghese.fc0.world;

import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.chunk.Chunk;
import tk.valoeghese.fc0.world.gen.GenWorld;
import tk.valoeghese.fc0.world.kingdom.Kingdom;
import tk.valoeghese.fc0.world.player.Player;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public interface TileAccess extends GenWorld {
	default void writeTile(TilePos pos, byte tile) {
		this.writeTile(pos.x, pos.y, pos.z, tile);
	}
	default byte readTile(TilePos pos) {
		return this.readTile(pos.x, pos.y, pos.z);
	}
	default byte readMeta(TilePos pos) {
		return this.readMeta(pos.x, pos.y, pos.z);
	}

	default int getHeight(int x, int z) {
		return this.getHeight(x, z, Tile::shouldRender);
	}

	Kingdom getKingdom(int x, int z);
	int getKingdomId(int x, int z);

	default boolean isInWorld(TilePos pos) {
		return this.isInWorld(pos.x, pos.y, pos.z);
	}

	void destroy();

	default void updateChunkOf(Player player) {
	}
}
