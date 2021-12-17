package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public abstract class PanelTest extends JPanel {
	protected abstract int getColour(int x, int z);

	public PanelTest size(int size) {
		this.size = size;
		return this;
	}

	protected double scale() {
		return 1.0;
	}

	private int size = 500;

	// drag
	private Point mousePt;
	protected int xo, lxo = 0;
	protected int yo, lyo = 0;

	public void start() {
		image = new BufferedImage(this.size, this.size, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < this.size; ++x) {
			for (int z = 0; z < this.size; ++z) {
				image.setRGB(x, z, this.getColour(x, z));
			}
		}

		// https://stackoverflow.com/questions/15574065/java-mousedragged-and-moving-around-in-a-graphical-interface
		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				mousePt = e.getPoint();
			}
		});

		this.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				int dx = e.getX() - mousePt.x;
				int dy = e.getY() - mousePt.y;
				xo -= dx;
				yo -= dy;
				mousePt = e.getPoint();
				redraw();
			}
		});

		JFrame frame = new JFrame();
		this.setPreferredSize(new Dimension(this.size, this.size));
		frame.add(this);
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

	public void redraw() {
		BufferedImage next = new BufferedImage(this.size, this.size, BufferedImage.TYPE_INT_RGB);
		BufferedImage current = image;

		for (int x = 0; x < this.size; ++x) {
			int actualX = x + xo;
			int oldX = actualX - lxo;

			for (int z = 0; z < this.size; ++z) {
				int actualZ = z + yo;
				int oldZ = actualZ - lyo;

				if (oldX >= 0 && oldX < this.size && oldZ >= 0 && oldZ < this.size) {
					next.setRGB(x, z, current.getRGB(oldX, oldZ));
				} else {
					next.setRGB(x, z, this.getColour(actualX, actualZ));
				}
			}
		}

		synchronized (this) {
			this.image = next;
			lxo = xo;
			lyo = yo;
		}
		super.repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.drawImage(image, 0, 0, null);
	}
}
