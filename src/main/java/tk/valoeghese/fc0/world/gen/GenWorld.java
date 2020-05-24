package tk.valoeghese.fc0.world.gen;

import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.function.Predicate;

public interface GenWorld {
	byte readTile(int x, int y, int z);
	void wgWriteTile(int x, int y, int z, byte tile);
	boolean isInWorld(int x, int y, int z);
	int getHeight(int x, int z, Predicate<Tile> solid);
}
