package tk.valoeghese.fc0.world.chunk;

public interface TileWriter {
	void writeMeta(int x, int y, int z, byte meta);
	void wgWriteTile(int x, int y, int z, byte tile);

	int WORLD_HEIGHT = 128;
}
