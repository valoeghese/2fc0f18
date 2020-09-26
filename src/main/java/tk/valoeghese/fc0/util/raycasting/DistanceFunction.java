package tk.valoeghese.fc0.util.raycasting;

import static java.lang.Math.max;
import static java.lang.Math.min;

public interface DistanceFunction {

	double length(Vec3d point);

	default DistanceFunction translate(Vec3d origin) {
		return point -> length(point.subtract(origin));
	}

	default DistanceFunction union(DistanceFunction function) {
		return point -> Math.min(length(point), function.length(point));
	}

	default DistanceFunction intersection(DistanceFunction function) {
		return point -> Math.max(length(point), function.length(point));
	}

	static DistanceFunction sphere(double radius) {
		return vec3d -> vec3d.length() - radius;
	}

	static DistanceFunction cuboid(double length, double width, double depth) {
		return point -> {
			double qx = Math.abs(point.x) - length;
			double qy = Math.abs(point.x) - width;
			double qz = Math.abs(point.x) - depth;
			return new Vec3d(max(qx, 0.0), max(qy, 0.0), max(qz, 0.0)).length() + min(max(qx, max(qy, qz)), 0.0);
		};
	}
}
