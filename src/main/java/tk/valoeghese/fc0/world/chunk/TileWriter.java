package tk.valoeghese.fc0.world.chunk;

public interface TileWriter {
	void writeMeta(int x, int y, int z, byte meta);
	void writeTile(int x, int y, int z, byte tile);

	byte readTile(int x, int y, int z);
	byte readMeta(int x, int y, int z);

	int WORLD_HEIGHT = 128;
}
