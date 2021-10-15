package test;

import tk.valoeghese.fc0.util.maths.Vec2f;
import tk.valoeghese.fc0.world.kingdom.Voronoi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public abstract class PanelTest extends JPanel {
	protected abstract int getColour(int x, int z);

	public void start() {
		image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < 500; ++x)
			for (int z = 0; z < 500; ++z) {
				image.setRGB(x, z, this.getColour(x, z));
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
		g.drawImage(image, 0, 0, null);
	}
}
