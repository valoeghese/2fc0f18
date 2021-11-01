package tk.valoeghese.fc0.world;

import tk.valoeghese.fc0.world.chunk.Chunk;
import tk.valoeghese.fc0.world.chunk.TileWriter;
import tk.valoeghese.fc0.world.chunk.ChunkLoadStatus;
import tk.valoeghese.fc0.world.gen.SeedWorld;
import tk.valoeghese.fc0.world.kingdom.Kingdom;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public interface ChunkAccess extends SeedWorld {
	@Nullable
	boolean loadChunk(int x, int z, ChunkLoadStatus status);
	/**
	 * Gets the chunk if it is currently loaded.
	 */
	@Nullable
	Chunk getChunk(int x, int z);
	/**
	 * Gets the chunk at LIGHT stage, if it exists.
	 */
	@Nullable
	Chunk getFullChunk(int x, int z);

	Kingdom kingdomById(int kingdom, int x, int z);
}
