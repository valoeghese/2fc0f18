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

	public static boolean withinBounds(double val, double bound0, double bound1, double falloff) {
		if (bound0 > bound1) {
			return withinBounds(val, bound1, bound0, falloff);
		}

		bound0 -= falloff;
		bound1 += falloff;
		System.out.println("bound0 " + bound0 + " bound1 " + bound1);

		return bound0 <= val && val <= bound1;
	}

	public static double distanceLineBetween(Vec2f start, Vec2f end, int x, int z) {
		return distanceLineBetween(start.getX(), start.getY(), end.getX(), end.getY(), x, z);
	}

	// Stolen from Khaki
	public static double distanceLineBetween(double startX, double startZ, double endX, double endZ, int x, int z) {
		double dx = endX - startX;
		double dz = endZ - startZ;

		// try fix bugs by swappings all x and z and doing it backwards
		if (Math.abs(dz) > Math.abs(dx)) {
			// cache old vals
			double oldDX = dx;
			double oldSX = startX;
			//double oldEX = endX; unused
			int oldX = x;

			// swap
			dx = dz;
			startX = startZ;
			//endX = endZ;
			x = z;

			dz = oldDX;
			startZ = oldSX;
			//endZ = oldEX;
			z = oldX;
		}

		double m = dz / dx;
		double targetZ = m * x + startZ - m * startX;
		return Math.abs(z - targetZ);
	}
}
