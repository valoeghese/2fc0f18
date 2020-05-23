package tk.valoeghese.fc0.util.noise;

import java.util.Random;

public class RidgedNoise extends Noise {
	public RidgedNoise(Random rand) {
		super(rand);
	}

	@Override
	public double sample(double x, double y) {
		return 1.0 - (Math.abs(super.sample(x, y)) * 2);
	}
}
