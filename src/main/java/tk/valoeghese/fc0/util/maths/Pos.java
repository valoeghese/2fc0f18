package tk.valoeghese.fc0.util.maths;

public class Pos {
	public Pos(Pos other) {
		this(other.x, other.y, other.z);
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
		double dx = Math.abs(other.x - this.x);
		double dy = Math.abs(other.y - this.y);
		double dz = Math.abs(other.z - this.z);
		return dx * dx + dy * dy + dz * dz;
	}

	@Override
	public String toString() {
		return "Pos(" + this.x
				+ ", " + this.y
				+ ", " + this.z
				+ ')';
	}
}
