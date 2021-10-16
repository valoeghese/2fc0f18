package tk.valoeghese.fc0.client.render.gui;

import tk.valoeghese.fc0.client.render.Textures;

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

			int cVal = (int) c - 32; // space is at 0

			int u;
			int v;

			switch (cVal) {
			case 196 - 32: // a umlaut capital
				u = 15;
				v = 5;
				break;
			case 203 - 32: // e
				u = 0;
				v = 6;
				break;
			case 207 - 32: // i
				u = 1;
				v = 6;
				break;
			case 214 - 32: // o
				u = 2;
				v = 6;
				break;
			case 220 - 32: // u
				u = 3;
				v = 6;
				break;
			default:
				u = cVal % 16;
				v = cVal / 16;
				break;
			}

			final float startU = (u / 16.0f);
			final float startV = (v / 16.0f);
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
				x += 0.63f * this.size;
			}
		}

		this.y1 = y;

		this.generateBuffers();
	}

	public static float widthOf(char[] text) {
		float width = 0;
		float prevMaxWidth = 0;

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

			if (c == ' ' || c == '`' || c == '.') {
				width += 0.43f * STEP;
			} else {
				width += 0.63f * STEP;
			}
		}

		return Math.max(width, prevMaxWidth);
	}

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
