package tk.valoeghese.fc0.world.gen;

import tk.valoeghese.fc0.world.chunk.TileWriter;
import tk.valoeghese.fc0.world.kingdom.Kingdom;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public interface GenWorld extends SeedWorld, TileWriter {
	/**
	 * Gets a means of access for chunks at the given location. Unloaded chunks will be loaded and have the modifications retroactively applied.<br/>
	 * Either returns a {@linkplain tk.valoeghese.fc0.world.chunk.Chunk chunk} or {@linkplain tk.valoeghese.fc0.world.chunk.OverflowChunk overflow chunk}.
	 */
	TileWriter getDelayedLoadChunk(int x, int z);

	@Override
	default void writeTile(int x, int y, int z, byte tile) {
		// removed canPlaceAt check for more direct writing. any "canPlaceAt" stuff should be done directly in the generator.
		this.getDelayedLoadChunk(x >> 4, z >> 4).writeTile(x, y, z, tile);
	}

	@Override
	default void writeMeta(int x, int y, int z, byte meta) {
		this.getDelayedLoadChunk(x >> 4, z >> 4).writeMeta(x, y, z, meta);
	}

	@Override
	default byte readTile(int x, int y, int z) {
		return this.getDelayedLoadChunk(x >> 4, z >> 4).readTile(x, y, z);
	}

	@Override
	default byte readMeta(int x, int y, int z) {
		return this.getDelayedLoadChunk(x >> 4, z >> 4).readMeta(x, y, z);
	}

	boolean isInWorld(int x, int y, int z);
	double sampleNoise(double x, double y);

	int getHeight(int x, int z, Predicate<Tile> solid);

	@Nullable
	Kingdom getKingdom(int x, int z);
}
