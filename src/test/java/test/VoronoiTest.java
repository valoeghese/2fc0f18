package test;

import tk.valoeghese.fc0.util.maths.Vec2f;
import tk.valoeghese.fc0.world.kingdom.Kingdom;
import tk.valoeghese.fc0.world.kingdom.Voronoi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class VoronoiTest extends PanelTest {
	public static void main(String[] args) {
		new VoronoiTest().start();
	}

	@Override
	protected int getColour(int x, int z) {
		Vec2f val = Voronoi.sampleVoronoi((float) x / 90.0f, (float) z / 90.0f, 123, Kingdom.RELAXATION);

		int color = 0;

		if (val.squaredDist((float) x / 90.0f, (float) z / 90.0f) > 0.001f) {
			color = val.hashCode() & 0xFF;
			color <<= 8;
			color |= (val.hashCode() * 3) & 0xFF;
			color <<= 8;
			color |= (val.hashCode() * 7) & 0xFF;
		}

		return color;
	}
}
