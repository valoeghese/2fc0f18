package tk.valoeghese.fc0.world;

public interface TileAccess {
	byte readTile(int x, int y, int z);
	void writeTile(int x, int y, int z, byte tile);
}
