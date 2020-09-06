package tk.valoeghese.fc0.world.gen.kingdom;

import tk.valoeghese.fc0.util.maths.MathsUtils;
import tk.valoeghese.fc0.util.maths.Vec2f;

public final class Voronoi {
	private static int random(int x, int y, int seed, int mask) {
		seed = 375462423 * seed + 672456235;
		seed += x;
		seed = 375462423 * seed + 672456235;
		seed += y;
		seed = 375462423 * seed + 672456235;
		return seed & mask;
	}

	private static float randomFloat(int x, int y, int seed) {
		return (float) random(x, y, seed, 0xFFFF) / (float) 0xFFFF;
	}

	public static Vec2f sample(float x, float y, int seed) {
		final int baseX = MathsUtils.floor(x);
		final int baseY = MathsUtils.floor(y);
		float rx = 0;
		float ry = 0;
		float rdist = 1000;

		for (int xo = -1; xo <= 1; ++xo) {
			int gridX = baseX + xo;

			for (int yo = -1; yo <= 1; ++yo) {
				int gridY = baseY + yo;

				// ensure more evenly distributed
				float vx = baseX + (randomFloat(gridX, gridY, seed) + 0.5f) * 0.5f;
				float vy = baseY + (randomFloat(gridX, gridY, seed + 1) + 0.5f) * 0.5f;
				float vdist = MathsUtils.squaredDist(x, y, vx, vy);

				if (vdist < rdist) {
					rx = vx;
					ry = vy;
					rdist = vdist;
				}
			}
		}

		return new Vec2f(rx, ry);
	}
}

