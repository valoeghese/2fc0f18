package tk.valoeghese.fc0.world.gen;

import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.gen.kingdom.Kingdom;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public interface GenWorld {
	boolean isInWorld(int x, int y, int z);
	@Nullable
	Chunk getChunk(int x, int z);
	double sampleNoise(double x, double y);

	default byte readTile(int x, int y, int z) {
		return this.getChunk(x >> 4, z >> 4).readTile(x & 0xF, y, z & 0xF);
	}

	default void wgWriteTile(int x, int y, int z, byte tile) {
		this.getChunk(x >> 4, z >> 4).writeTile(x & 0xF, y, z & 0xF, tile);
	}

	default int getHeight(int x, int z, Predicate<Tile> solid) {
		return this.getChunk(x >> 4, z >> 4).getHeight(x & 0xF, z & 0xF, solid);
	}

	default void writeMeta(int x, int y, int z, byte meta) {
		this.getChunk(x >> 4, z >> 4).writeMeta(x & 0xF, y, z & 0xF, meta);
	}

	default byte readMeta(int x, int y, int z) {
		return this.getChunk(x >> 4, z >> 4).readMeta(x & 0xF, y, z & 0xF);
	}

	default Kingdom getKingdom(int x, int z) {
		return this.getChunk(x >> 4, z >> 4).getKingdom(x & 0xF, z & 0xF);
	}

	default int getKingdomId(int x, int z) {
		return this.getChunk(x >> 4, z >> 4).getKingdomId(x & 0xF, z & 0xF);
	}
}
