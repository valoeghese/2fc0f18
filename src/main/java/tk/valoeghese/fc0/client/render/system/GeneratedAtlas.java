package tk.valoeghese.fc0.client.render.system;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class GeneratedAtlas {
	public GeneratedAtlas(String name, ImageEntry[] images) {
		int x = 0;
		int y = 0;
		this.image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

		for (ImageEntry iEntry : images) {
			BufferedImage image = iEntry.image;
			++x;

			if (x > 16) {
				x = 0;
				++y;
			}

			if (y > 16) {
				throw new RuntimeException("Generated Atlas \"" + name + "\" is too large!");
			}

			if (image.getWidth() != 16 || image.getHeight() != 16) {
				throw new RuntimeException("Invalidly sized image encountered while generating atlas \"" + name + "\"!");
			}

			this.imageLocationMap.put(iEntry.name, new ImageUV(x, y));
			int startX = x << 4;
			int startY = y << 4;

			for (int xo = 0; xo < 16; ++xo) {
				int totalX = startX + xo;

				for (int yo = 0; yo < 16; ++yo) {
					int totalY = startY + yo;

					this.image.setRGB(totalX, totalY, image.getRGB(xo, yo));
				}
			}
		}

		this.name = name;
		System.out.println("Successfully Generated Atlas \"" + name + "\"");
	}

	public final String name;
	public final BufferedImage image;
	public final Map<String, ImageUV> imageLocationMap = new HashMap<>();

	public static class ImageEntry {
		public String name;
		public BufferedImage image;
	}

	public static class ImageUV {
		private ImageUV(int x, int y) {
			this.x = x;
			this.y = y;
		}

		private int x;
		private int y;

		public int getX() {
			return this.x;
		}

		public int getY() {
			return this.y;
		}
	}
}
