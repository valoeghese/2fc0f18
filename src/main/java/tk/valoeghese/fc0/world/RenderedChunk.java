package tk.valoeghese.fc0.world;

import tk.valoeghese.fc0.world.tile.Tile;

public interface RenderedChunk {
	boolean renderHeight(int y);
	Tile north(int x, int y);
	Tile south(int x, int y);
	Tile east(int z, int y);
	Tile west(int z, int y);
}
