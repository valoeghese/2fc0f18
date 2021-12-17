package tk.valoeghese.fc0.world.kingdom;

import tk.valoeghese.fc0.util.maths.Vec2f;

public final class Voronoi {
	public static Vec2f sampleVoronoiGrid(int x, int y, int seed, float relaxation) {
		float unrelaxation = 1.0f - relaxation;
		float vx = x + relaxation * 0.5f + unrelaxation * randomfloat(x, y, seed);
		float vy = y + relaxation * 0.5f + unrelaxation * randomfloat(x, y, seed + 1);
		return new Vec2f(vx, vy);
	}

	public static Vec2f sampleVoronoi(float x, float y, int seed, float relaxation) {
		float unrelaxation = 1.0f - relaxation;

		final int baseX = (int) Math.floor(x);
		final int baseY = (int) Math.floor(y);
		float rx = 0;
		float ry = 0;
		float rdist = 1000;

		for (int xo = -1; xo <= 1; ++xo) {
			int gridX = baseX + xo;

			for (int yo = -1; yo <= 1; ++yo) {
				int gridY = baseY + yo;

				float vx = gridX + relaxation * 0.5f + unrelaxation * randomfloat(gridX, gridY, seed);
				float vy = gridY + relaxation * 0.5f + unrelaxation * randomfloat(gridX, gridY, seed + 1);
				float vdist = squaredDist(x, y, vx, vy);

				if (vdist < rdist) {
					rx = vx;
					ry = vy;
					rdist = vdist;
				}
			}
		}

		return new Vec2f(rx, ry);
	}

	public static float sampleD1D2SquaredWorley(float x, float y, int seed) {
		final int baseX = (int) Math.floor(x);
		final int baseY = (int) Math.floor(y);
		float rdist2 = 1000;
		float rdist = 1000;

		for (int xo = -1; xo <= 1; ++xo) {
			int gridX = baseX + xo;

			for (int yo = -1; yo <= 1; ++yo) {
				int gridY = baseY + yo;

				float vx = gridX + randomfloat(gridX, gridY, seed);
				float vy = gridY + randomfloat(gridX, gridY, seed + 1);
				float vdist = squaredDist(x, y, vx, vy);

				if (vdist < rdist) {
					rdist2 = rdist;
					rdist = vdist;
				} else if (vdist < rdist2) {
					rdist2 = vdist;
				}
			}
		}

		return rdist2 - rdist;
	}

	//public static void main(String[] args) {
	//	System.out.println(sampleD1D2Worley(0, 1, 5));
	//}

	public static float sampleEvenD1SquaredWorley(float x, float y, int seed) {
		final int baseX = (int) Math.floor(x);
		final int baseY = (int) Math.floor(y);
		float dist = 1000;

		for (int xo = -1; xo <= 1; ++xo) {
			int gridX = baseX + xo;

			for (int yo = -1; yo <= 1; ++yo) {
				int gridY = baseY + yo;

				// ensure more evenly distributed
				float vx = gridX + (randomfloat(gridX, gridY, seed) + 0.5f) * 0.5f;
				float vy = gridY + (randomfloat(gridX, gridY, seed + 1) + 0.5f) * 0.5f;
				float vdist = squaredDist(x, y, vx, vy);

				if (vdist < dist) {
					dist = vdist;
				}
			}
		}

		return dist;
	}

	public static float sampleD1SquaredWorley(float x, float y, int seed) {
		final int baseX = (int) Math.floor(x);
		final int baseY = (int) Math.floor(y);
		float dist = 1000;

		for (int xo = -1; xo <= 1; ++xo) {
			int gridX = baseX + xo;

			for (int yo = -1; yo <= 1; ++yo) {
				int gridY = baseY + yo;

				float vx = gridX + randomfloat(gridX, gridY, seed);
				float vy = gridY + randomfloat(gridX, gridY, seed + 1);
				float vdist = squaredDist(x, y, vx, vy);

				if (vdist < dist) {
					dist = vdist;
				}
			}
		}

		return dist;
	}

	public static Vec2f sampleManhattanVoronoi(float x, float y, int seed) {
		final int baseX = (int) Math.floor(x);
		final int baseY = (int) Math.floor(y);
		float rx = 0;
		float ry = 0;
		float rdist = 1000;

		for (int xo = -1; xo <= 1; ++xo) {
			int gridX = baseX + xo;

			for (int yo = -1; yo <= 1; ++yo) {
				int gridY = baseY + yo;

				float vx = gridX + randomfloat(gridX, gridY, seed);
				float vy = gridY + randomfloat(gridX, gridY, seed + 1);
				float vdist = manhattanDist(x, y, vx, vy);

				if (vdist < rdist) {
					rx = vx;
					ry = vy;
					rdist = vdist;
				}
			}
		}

		return new Vec2f(rx, ry);
	}

	public static int random(int x, int y, int seed, int mask) {
		seed *= 375462423 * seed + 672456235;
		seed += x;
		seed *= 375462423 * seed + 672456235;
		seed += y;
		seed *= 375462423 * seed + 672456235;
		seed += x;
		seed *= 375462423 * seed + 672456235;
		seed += y;

		return seed & mask;
	}

	private static float squaredDist(float x0, float y0, float x1, float y1) {
		float dx = x1 - x0;
		float dy = y1 - y0;
		return dx * dx + dy * dy;
	}

	private static float manhattanDist(float x0, float y0, float x1, float y1) {
		float dx = Math.abs(x1 - x0);
		float dy = Math.abs(y1 - y0);
		return dx + dy;
	}

	public static float randomfloat(int x, int y, int seed) {
		return (float) random(x, y, seed, 0xFFFF) / (float) 0xFFFF;
	}
}