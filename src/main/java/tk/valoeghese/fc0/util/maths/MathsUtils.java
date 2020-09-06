package tk.valoeghese.fc0.util.maths;

public class MathsUtils {
	private MathsUtils() {
		// NO-OP
	}

	public static float clampMap(float value, float min, float max, float newmin, float newmax) {
		value -= min;
		value /= (max - min);
		value = newmin + value * (newmax - newmin);

		if (value > newmax) {
			return newmax;
		} else if (value < newmin) {
			return newmin;
		} else {
			return value;
		}
	}

	public static int clamp(int i, int low, int high) {
		return i < low ? low : (i > high ? high : i);
	}

	public static int sign(double d) {
		if (d == 0) {
			return 0;
		}

		return d > 0 ? 1 : -1;
	}

	public static int floor(float f) {
		int i = (int) f;
		return f < i ? i - 1 : i;
	}

	public static int manhattan(int x0, int y0, int x1, int y1) {
		int dx = Math.abs(x1 - x0);
		int dy = Math.abs(y1 - y0);
		return dx + dy;
	}

	public static float squaredDist(float x0, float y0, float x1, float y1) {
		float dx = Math.abs(x1 - x0);
		float dy = Math.abs(y1 - y0);
		return dx * dx + dy * dy;
	}

	public static int floor(double f) {
		return (int) Math.floor(f);
	}
}
