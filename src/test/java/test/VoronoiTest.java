package test;

import tk.valoeghese.fc0.util.maths.Vec2f;
import tk.valoeghese.fc0.world.kingdom.Voronoi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class VoronoiTest extends JPanel {
	public static void main(String[] args) {
		image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < 500; ++x)
			for (int z = 0; z < 500; ++z) {
				Vec2f val = Voronoi.sampleVoronoi((float) x / 90.0f, (float) z / 90.0f, 123, 0.5f);

				int color = 0;

				if (val.squaredDist((float) x / 90.0f, (float) z / 90.0f) > 0.001f) {
					color = val.hashCode() & 0xFF;
					color <<= 8;
					color |= (val.hashCode() * 3) & 0xFF;
					color <<= 8;
					color |= (val.hashCode() * 7) & 0xFF;
				}

				image.setRGB(x, z, color);
			}

		JFrame frame = new JFrame();
		JPanel panel = new VoronoiTest();
		panel.setPreferredSize(new Dimension(500, 500));
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	static BufferedImage image;

	@Override
	protected void paintComponent(Graphics g) {
		int width = this.getWidth();
		int height = this.getHeight();
		g.drawImage(image, 0, 0, null);
	}
}
