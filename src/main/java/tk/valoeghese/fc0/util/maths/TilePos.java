package tk.valoeghese.fc0.util.maths;

import java.util.Objects;

public class TilePos {
	public TilePos(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public TilePos(float x, float y, float z) {
		this(MathsUtils.floor(x), MathsUtils.floor(y), MathsUtils.floor(z));
	}

	public TilePos(Pos pos) {
		this(MathsUtils.floor(pos.x), MathsUtils.floor(pos.y), MathsUtils.floor(pos.z));
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

	public ChunkPos toChunkPos() {
		return new ChunkPos(this.x >> 4, this.z >> 4);
	}

	public TilePos ofAdded(int x, int y, int z) {
		return new TilePos(this.x + x, this.y + y, this.z + z);
	}

	public int horizontalManhattanDist(int x, int z) {
		int dx = Math.abs(x - this.x);
		int dz = Math.abs(z - this.z);
		return dx + dz;
	}

	@Override
	public String toString() {
		return "TilePos(" + this.x
				+ ", " + this.y
				+ ", " + this.z
				+ ')';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TilePos tilePos = (TilePos) o;
		return this.x == tilePos.x && this.y == tilePos.y && this.z == tilePos.z;
	}

	@Override
	public int hashCode() {
		int result = this.x;
		result = 31 * result + this.y;
		result = 31 * result + this.z;
		return result;
	}
}
