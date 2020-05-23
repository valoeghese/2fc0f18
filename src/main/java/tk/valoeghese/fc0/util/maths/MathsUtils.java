package tk.valoeghese.fc0.util.maths;

public final class MathsUtils {
	public static int sign(double d) {
		if (d == 0) {
			return 0;
		}

		return d > 0 ? 1 : -1;
	}
}
