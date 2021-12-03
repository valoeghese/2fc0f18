package tk.valoeghese.fc0.client.render.gui;

import tk.valoeghese.fc0.client.render.Textures;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.Buffer;
import java.util.Arrays;

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
		final boolean debug = false; // Created when upgrading the font renderer to debug issues with the upgraded version. Instead of calculating proportions for alphabetic characters, dumps an image. Please note this will result in alphanumeric characters not being located correctly in-game.
		BufferedImage debugImage = null;
		String debugFileStr = null;

		for (int u = 0; u < 16; ++u) {
			for (int v = 0; v < 16; ++v) {
				if (debug) {
					char c = (char)((v * 16) + u + 32);
					if (Character.isAlphabetic(c) && c <= 'z') {
						debugFileStr = c + "_uv.png";
						debugImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
					}
				}

				int checkU = u << 4;
				int startV = v << 4;
				boolean bronk = false; // whether to be allowed to bronk (break)

				checker:
				for (int du = 0; du < 16; ++du, ++checkU) {
					for (int dv = 0; dv < 16; ++dv) {
						if (debugImage != null) {
							debugImage.setRGB(du, 15 - dv, image.getRGB(checkU, 255 - (startV + dv)));
						} else {
							if ((image.getRGB(checkU, 255 - (startV + dv)) & 0xFF000000) != 0) {
								bronk = true; // now it's allowed to break
								continue checker; // next column
							}
						}
					}

					if (bronk) {
						proportions[u][v] = 0.67f * (float) (du + 1) / 15.0f;
						break checker; // stop
					}
				}

				if (debugImage != null) {
					try {
						ImageIO.write(debugImage, "png", new File("saves/" + debugFileStr));
					} catch (IOException e) {
						throw new UncheckedIOException("Error Debugging Font Renderer", e);
					}
					debugImage = null;
				}
				// if gone the whole way and has been full
				else if (bronk && proportions[u][v] == 0.0f) {
					proportions[u][v] = 0.67f * 16.0f / 15.0f; // max size
				}
			}
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

	private static final float STEP = 0.05f;
}
