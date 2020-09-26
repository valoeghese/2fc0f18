package tk.valoeghese.fc0.util.raycasting;

import tk.valoeghese.fc0.util.Pair;

public class RayCasting {

	/**
	 * Casts a ray
	 *
	 * @param start     The starting point of the ray
	 * @param direction The normalized direction vector of this ray
	 * @param maxLength The maximum length to travel
	 * @param function  The distance function
	 * @return A pair of the final position of the ray and if the ray ever hit anything
	 */
	public static Pair<Pos, Boolean> rayCast(Pos start, Pos direction, double maxLength, DistanceFunction function) {
		Pos lastPoint = start;
		Pos point = lastPoint;
		double rayLength = 0;

		do {
			double length = function.length(point);

			if (isZero(length)) {
				return new Pair<>(point, true);
			} else {
				rayLength += length;
				lastPoint = point;
				point = point.add(direction.multiply(length));
			}
		} while (rayLength < maxLength);

		return new Pair<>(lastPoint, false);
	}

	private static boolean isZero(double d) {
		return Math.abs(d) < 1E-8D;
	}
}
