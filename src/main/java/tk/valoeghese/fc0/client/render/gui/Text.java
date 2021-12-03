package tk.valoeghese.fc0.client.render.gui;

import tk.valoeghese.fc0.client.render.Textures;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;

public class Text extends GUI {
	public Text(String value, float xOffset, float yOffset, float size) {
		super(Textures.FONT_ATLAS);

		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.size = STEP * size;
		this.text = value;
		this.setText(value.toCharArray());
	}

	protected float xOffset;
	protected float yOffset;
	protected String text;
	protected final float size;
	protected float x1, y1;

	public void changeText(String newText) {
		this.destroy();
		this.text = newText;
		this.setText(newText.toCharArray());
	}

	private void setText(char[] text) {
		float x = this.xOffset;
		float y = this.yOffset;
		int[] uv = new int[2];

		for (char c : text) {
			if (c == '\n') {
				if (x > this.x1) {
					this.x1 = x;
				}

				y -= this.size;
				x = this.xOffset;
				continue;
			}

			if (c < 32) {
				throw new RuntimeException("Text cannot contain non-newline characters lower than 32!");
			}

			getUV(c, uv);

			final float startU = (uv[0] / 16.0f);
			final float startV = (uv[1] / 16.0f);
			final float endU = startU + 0.0625f;
			final float endV = startV + 0.0625f;

			int tl = this.vertex(x, y + this.size, startU, endV);
			int bl = this.vertex(x, y, startU, startV);
			int tr = this.vertex(x + (0.73f * this.size), y + this.size, endU, endV);
			int br = this.vertex(x + (0.73f * this.size), y, endU, startV);

			this.tri(tl, bl, br);
			this.tri(tl, tr, br);

			if (c == ' ' || c == '`' || c == '.') {
				x += 0.43f * this.size;
			} else {
				//System.out.println(proportions[uv[0]][uv[1]]);
				x += proportions[uv[0]][uv[1]] * this.size;
			}
		}

		this.y1 = y;

		this.generateBuffers();
	}

	private static void getUV(char c, int[] uv) {
		int cVal = (int) c - 32; // space is at 0

		switch (cVal) {
		case 196 - 32: // a umlaut capital
			uv[0] = 15;
			uv[1] = 5;
			break;
		case 203 - 32: // e
			uv[0] = 0;
			uv[1] = 6;
			break;
		case 207 - 32: // i
			uv[0] = 1;
			uv[1] = 6;
			break;
		case 214 - 32: // o
			uv[0] = 2;
			uv[1] = 6;
			break;
		case 220 - 32: // u
			uv[0] = 3;
			uv[1] = 6;
			break;
		default:
			uv[0] = cVal % 16;
			uv[1] = cVal / 16;
			break;
		}
	}

	/**
	 * Width of text for Size=2.0f
	 * @param text the text to measure the width of.
	 * @return the width, as a float such as those the GUI code uses.
	 */
	public static float widthOf(char[] text) {
		float width = 0;
		float prevMaxWidth = 0;
		int[] uv = new int[2];

		for (char c : text) {
			if (c == '\n') {
				if (width > prevMaxWidth) {
					prevMaxWidth = width;
				}

				width = 0;
				continue;
			}

			if (c < 32) {
				throw new RuntimeException("Text cannot contain non-newline characters lower than 32!");
			}

			if (c == ' ') {
				width += 0.67 * STEP;
			} else {
				getUV(c, uv);
				width += proportions[uv[0]][uv[1]] * STEP;
			}
		}

		return Math.max(width, prevMaxWidth);
	}

	/**
	 * Calulates (or re-calculates) distance proportions of text.
	 */
	public static void calculateProportions(BufferedImage image) {
		BufferedImage bi = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

		for (int u = 0; u < 16; ++u) {
			for (int v = 0; v < 16; ++v) {
				int checkU = u << 4;
				int startV = v << 4;

				checker:
				for (int du = 0; du < 16; ++du, ++checkU) {
					for (int dv = 0; dv < 16; ++dv) {
						int irgb = image.getRGB(checkU, 255 - (startV + dv));
						bi.setRGB(checkU, 255 - (startV + dv), irgb);
						if ((irgb >> 24) > 0) {
							//bi.setRGB(checkU, 255-startV-dv, 0xFFFFFFFF);
							//continue checker; // next column
						} else {
							//bi.setRGB(checkU, 255-startV-dv, 0xFF000000);
						}

						//proportions[u][v] = (float)(du + 1) / 15.0f;
						//break checker; // stop
					}
				}
			}
		}

		try {
			ImageIO.write(bi, "png", new File("test.png"));
		} catch (IOException e) {
			throw new RuntimeException("awuvweagh w", e);
		}
	}

	private static float[][] proportions = new float[16][16];

	public static class Moveable extends Text {
		public Moveable(String value, float xOffset, float yOffset, float size) {
			super(value, xOffset, yOffset, size);
		}

		public void setOffsets(float xOffset, float yOffset) {
			this.xOffset = xOffset;
			this.yOffset = yOffset;
			this.changeText(this.text);
		}

		public void changeText(String text, float xOffset, float yOffset) {
			this.xOffset = xOffset;
			this.yOffset = yOffset;
			this.changeText(text);
		}

		public float getXOffset() {
			return this.xOffset;
		}

		public float getYOffset() {
			return this.yOffset;
		}
	}

	private static final float STEP = 0.03f;
}
