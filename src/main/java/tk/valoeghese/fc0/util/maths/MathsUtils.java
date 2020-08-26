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
		return (int) Math.floor(f);
	}

	public static int floor(double f) {
		return (int) Math.floor(f);
	}
}
