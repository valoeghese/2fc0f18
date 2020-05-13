package tk.valoeghese.fc0.util;

public class TilePos {
	public TilePos(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public TilePos(Pos pos) {
		this((int) (pos.x + 0.5), (int) (pos.y + 0.5), (int) (pos.z + 0.5));
	}

	public final int x;
	public final int y;
	public final int z;

	public boolean isValidForChunk() {
		return this.x >= 0 && this.x < 16 && this.z >= 0 && this.z < 16 && this.y >= 0 && this.y < 128;
	}
}
