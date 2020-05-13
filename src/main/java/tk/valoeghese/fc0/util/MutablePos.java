package tk.valoeghese.fc0.util;

public class MutablePos extends Pos {
	public MutablePos(double x, double y, double z) {
		super(x, y, z);
	}

	public MutablePos setX(double x) {
		this.x = x;
		return this;
	}

	public MutablePos setY(double y) {
		this.y = y;
		return this;
	}

	public MutablePos setZ(double z) {
		this.z = z;
		return this;
	}

	public MutablePos offsetX(double x) {
		this.x += x;
		return this;
	}

	public MutablePos offsetY(double y) {
		this.y += y;
		return this;
	}

	public MutablePos offsetZ(double z) {
		this.z += z;
		return this;
	}

	public MutablePos set(Pos other) {
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
		return this;
	}
}
