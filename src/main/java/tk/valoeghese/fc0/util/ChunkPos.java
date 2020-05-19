package tk.valoeghese.fc0.util;

public class ChunkPos {
	public ChunkPos(int x, int z) {
		this.x = x;
		this.z = z;
	}

	public final int x;
	public final int z;

	public TilePos toTilePos(int y) {
		return new TilePos(this.x << 4, y, this.z << 4);
	}
}
