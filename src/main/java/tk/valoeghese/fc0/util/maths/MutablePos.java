package tk.valoeghese.fc0.util.maths;

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
		return this.set(other.x, other.y, other.z);
	}

	public MutablePos set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public MutablePos offset(Pos pos) {
		return this.offset(pos.x, pos.y, pos.z);
	}

	public MutablePos offset(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public MutablePos mul(double x, double y, double z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		return this;
	}
}
