package tk.valoeghese.fc0.util.raycasting;

import java.util.Objects;

import org.joml.Vector3dc;

public class Vec3d {

	public final double x, y, z;

	public Vec3d(Vector3dc v) {
		this(v.x(), v.y(), v.z());
	}

	public Vec3d(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3d add(Vec3d v) {
		return new Vec3d(x + v.x, y + v.y, z + v.z);
	}

	public Vec3d subtract(Vec3d v) {
		return new Vec3d(x - v.x, y - v.y, z - v.z);
	}

	public Vec3d multiply(double scalar) {
		return new Vec3d(x * scalar, y * scalar, z * scalar);
	}

	public Vec3d normalize() {
		double length = length();
		return new Vec3d(x / length, y / length, z / length);
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

		Vec3d vec3d = (Vec3d) o;
		return Double.compare(vec3d.x, x) == 0 &&
				Double.compare(vec3d.y, y) == 0 &&
				Double.compare(vec3d.z, z) == 0;
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
