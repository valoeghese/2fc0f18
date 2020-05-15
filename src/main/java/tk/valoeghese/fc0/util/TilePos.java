package tk.valoeghese.fc0.util;

public class TilePos {
	public TilePos(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public TilePos(Pos pos) {
		this((int) Math.floor(pos.x + 0.5), (int) Math.floor(pos.y + 0.5), (int) Math.floor(pos.z + 0.5));
	}

	public final int x;
	public final int y;
	public final int z;

	public boolean isValidForChunk() {
		return this.x >= 0 && this.x < 16 && this.z >= 0 && this.z < 16 && this.y >= 0 && this.y < 128;
	}

	public TilePos up() {
		return new TilePos(this.x, this.y + 1, this.z);
	}

	public TilePos down() {
		return new TilePos(this.x, this.y - 1, this.z);
	}

	public TilePos ofAdded(int x, int y, int z) {
		return new TilePos(this.x + x, this.y + y, this.z + z);
	}
}
