package tk.valoeghese.fc0.util.maths;

import java.util.Objects;

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

	public double distanceTo(ChunkPos other) {
		int dx = other.x - this.x;
		int dz = other.z - this.z;
		return Math.sqrt(dx * dx + dz * dz);
	}

	public int manhattan(ChunkPos other) {
		return this.manhattan(other.x, other.z);
	}

	public int manhattan(int x, int z) {
		int dx = Math.abs(x - this.x);
		int dz = Math.abs(z - this.z);
		return dx + dz;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}

		ChunkPos chunkPos = (ChunkPos) o;

		return this.x == chunkPos.x && this.z == chunkPos.z;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.x, this.z);
	}

	@Override
	public String toString() {
		return "ChunkPos(" + this.x
				+ ", " + this.z
				+ ')';
	}
}
