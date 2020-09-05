package tk.valoeghese.fc0.util.maths;

public class Vec2f {
	public Vec2f(Vec2f other) {
		this(other.x, other.y);
	}

	public Vec2f(float x, float y) {
		this.x = x;
		this.y = y;
	}

	protected float x;
	protected float y;

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public Vec2f ofAdded(float x, float y) {
		return new Vec2f(this.x + x, this.y + y);
	}

	public Vec2f ofAdded(Vec2f other) {
		return this.ofAdded(other.x, other.y);
	}

	public float squaredDist(Vec2f other) {
		return this.squaredDist(other.x, other.y);
	}

	public float squaredDist(float x, float y) {
		float dx = Math.abs(x - this.x);
		float dy = Math.abs(y - this.y);
		return dx * dx + dy * dy;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o == null) {
			return false;
		} else if (o instanceof Vec2f) {
			Vec2f vec2f = (Vec2f) o;
			return vec2f.x == this.x && vec2f.y == this.y;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int result = 7;
		result = 29 * result + Float.hashCode(this.x);
		result = 29 * result + Float.hashCode(this.y);
		return result;
	}

	@Override
	public String toString() {
		return "Vec2f(" + this.x
				+ ", " + this.y
				+ ')';
	}
}
