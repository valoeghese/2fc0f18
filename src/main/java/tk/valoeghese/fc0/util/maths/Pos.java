package tk.valoeghese.fc0.util.maths;

public class Pos {
	public Pos(Pos other) {
		this(other.x, other.y, other.z);
	}

	public Pos(Pos first, Pos second, double lerp) {
		this(
			MathsUtils.lerp(first.x, second.x, lerp),
			MathsUtils.lerp(first.y, second.y, lerp),
			MathsUtils.lerp(first.z, second.z, lerp)
		);
	}

	public Pos(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	protected double x;
	protected double y;
	protected double z;

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public Pos ofAdded(double x, double y, double z) {
		return new Pos(this.x + x, this.y + y, this.z + z);
	}

	public Pos ofAdded(Pos other) {
		return this.ofAdded(other.x, other.y, other.z);
	}

	public double squaredDist(Pos other) {
		double dx = other.x - this.x;
		double dy = other.y - this.y;
		double dz = other.z - this.z;
		return dx * dx + dy * dy + dz * dz;
	}

	public double squaredLength() {
		return this.x * this.x + this.y * this.y + this.z * this.z;
	}

	@Override
	public String toString() {
		return "Pos(" + this.x
				+ ", " + this.y
				+ ", " + this.z
				+ ')';
	}

	public static final Pos ZERO = new Pos(0.0, 0.0, 0.0);
}
