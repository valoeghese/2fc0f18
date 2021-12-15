package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public abstract class PanelTest extends JPanel {
	protected abstract int getColour(int x, int z);

	public PanelTest size(int size) {
		this.size = size;
		return this;
	}

	private int size = 500;

	public void start() {
		image = new BufferedImage(this.size, this.size, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < this.size; ++x)
			for (int z = 0; z < this.size; ++z) {
				image.setRGB(x, z, this.getColour(x, z));
			}

		JFrame frame = new JFrame();
		JPanel panel = new VoronoiTest();
		panel.setPreferredSize(new Dimension(this.size, this.size));
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
