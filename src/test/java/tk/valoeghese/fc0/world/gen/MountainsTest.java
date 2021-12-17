package tk.valoeghese.fc0.world.gen;

import test.PanelTest;

import java.awt.*;
import java.util.Random;

/**
 * Displays a heightmap of a world where 1 pixel = 4 blocks (not average, picks instead the first in the block selection)
 */
public class MountainsTest extends PanelTest {
	public static void main(String[] args) {
		new MountainsTest().scale(6).start();
	}

	static WorldGen.Earth worldGen = new WorldGen.Earth(new Random().nextLong(), 0);

	@Override
	protected int getColour(int x, int z) {
		float height = 0.5f + (float) worldGen.sampleMountains(x, z) * 0.005f;
		return Color.getHSBColor(0.0f, 0.0f, height > 1.0f? 1.0f:height).getRGB();
	}
}
