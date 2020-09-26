package tk.valoeghese.fc0.util.raycasting;

public class RayCasting {

	public static Vec3d rayCast(Vec3d from, Vec3d to, DistanceFunction function) {
		Vec3d v = to.subtract(from);
		return rayCast(from, v.normalize(), v.length(), function);
	}

	public static Vec3d rayCast(Vec3d start, Vec3d direction, double maxLength, DistanceFunction function) {
		Vec3d lastPoint = start;
		Vec3d point = lastPoint;
		double rayLength = 0;

		do {
			double length = function.length(point);

			if (isZero(length)) {
				return point;
			} else {
				rayLength += length;
				lastPoint = point;
				point = point.add(direction.multiply(length));
			}
		} while (rayLength < maxLength);

		return lastPoint;
	}

	private static boolean isZero(double d) {
		return Math.abs(d) < 1E-8D;
	}
}
