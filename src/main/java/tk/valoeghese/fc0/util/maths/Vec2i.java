package tk.valoeghese.fc0.util.maths;

import tk.valoeghese.fc0.client.render.system.GeneratedAtlas;

import java.util.Objects;

public class Vec2i {
	public Vec2i(GeneratedAtlas.ImageUV loc) {
		this(loc == null ? 0 : loc.getX(), loc == null ? 0 : loc.getY());
	}

	public Vec2i(int x, int y) {
		this.x = x;
		this.y = y;
	}

	private final int x;
	private final int y;

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public int manhattan(int x, int y) {
		return MathsUtils.manhattan(this.x, this.y, x, y);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}

		Vec2i other = (Vec2i) o;

		return this.x == other.x && this.y == other.y;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.x, this.y, 3);
	}

	@Override
	public String toString() {
		return "Vec2i(" + this.x
				+ ", " + this.y
				+ ')';
	}
}
