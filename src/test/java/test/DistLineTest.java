package test;

import tk.valoeghese.fc0.util.maths.MathsUtils;
import tk.valoeghese.fc0.util.maths.Vec2f;

import java.awt.*;
import java.util.Random;

public class DistLineTest extends PanelTest {
	public static void main(String[] args) {
		new DistLineTest().start();
	}

	@Override
	public void start() {
		Random random = new Random();
		this.point0 = new Vec2f(random.nextInt(250), random.nextInt(250));
		this.point1 = new Vec2f(random.nextInt(250) + 250, random.nextInt(250));
		this.point2 = new Vec2f(random.nextInt(250), random.nextInt(250) + 250);
		this.point3 = new Vec2f(random.nextInt(250)+ 250, random.nextInt(500) + 250);
		super.start();
	}


	Vec2f point0, point1, point2, point3;

	@Override
	protected int getColour(int x, int z) {
		double grey = Math.min(
				Math.min(
						MathsUtils.distanceLineBetween(this.point0, this.point1, x, z),
						MathsUtils.distanceLineBetween(this.point0, this.point2, x, z)
				),
				Math.min(
						MathsUtils.distanceLineBetween(this.point3, this.point1, x, z),
						MathsUtils.distanceLineBetween(this.point3, this.point2, x, z)
				)
		);

//		I was trying stuff out and this looks tripply lmao
//		double grey =
//				MathsUtils.distanceLineBetween(this.point0, this.point1, x, z) -
//				MathsUtils.distanceLineBetween(this.point0, this.point2, x, z) -
//				MathsUtils.distanceLineBetween(this.point3, this.point1, x, z) -
//				MathsUtils.distanceLineBetween(this.point3, this.point2, x, z);

		if (grey > 40.0) {
			return 0;
		} else {
			Color color = Color.getHSBColor(0.0f, 0.0f, 1.0f - (float) grey / 40.0f);
			return color.getRGB();
		}
	}
}
