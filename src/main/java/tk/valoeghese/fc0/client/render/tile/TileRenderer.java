package tk.valoeghese.fc0.client.render.tile;

import tk.valoeghese.fc0.client.render.model.ChunkMesh;
import tk.valoeghese.fc0.client.world.RenderedChunk;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.List;

/**
 * An interface for custom rendering of tiles.
 */
public interface TileRenderer {
	/**
	 * @param instance the tile to render
	 * @param layer the layer of rendered tile faces to add faces to
	 * @param tiles the array of tiles for rendering.
	 * @param chunk the chunk
	 * @param x the local chunk x
	 * @param y the local chunk y
	 * @param z the local chunk z
	 * @param meta the meta value of the tile
	 */
	void addFaces(Tile instance, List<ChunkMesh.RenderedTileFace> layer, byte[] tiles, RenderedChunk chunk, int x, int y, int z, byte meta);

	/**
	 * @see tk.valoeghese.fc0.world.Chunk#index
	 */
	static int index(int x, int y, int z) {
		return (x << 11) | (z << 7) | y;
	}
}
