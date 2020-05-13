package tk.valoeghese.fc0.util;

public class Pos {
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
}
