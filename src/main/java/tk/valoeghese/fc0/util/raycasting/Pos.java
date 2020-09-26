package tk.valoeghese.fc0.util.raycasting;

import java.util.Objects;

import org.joml.Vector3dc;

public class Pos {

	public final double x, y, z;

	public Pos(Vector3dc v) {
		this(v.x(), v.y(), v.z());
	}

	public Pos(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Pos add(Pos v) {
		return new Pos(x + v.x, y + v.y, z + v.z);
	}

	public Pos subtract(Pos v) {
		return new Pos(x - v.x, y - v.y, z - v.z);
	}

	public Pos multiply(double scalar) {
		return new Pos(x * scalar, y * scalar, z * scalar);
	}

	public Pos normalize() {
		double length = length();
		return new Pos(x / length, y / length, z / length);
	}

	public double length() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Pos pos = (Pos) o;
		return Double.compare(pos.x, x) == 0 &&
				Double.compare(pos.y, y) == 0 &&
				Double.compare(pos.z, z) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{" +
				"x=" + x +
				", y=" + y +
				", z=" + z +
				'}';
	}
}
