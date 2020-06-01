package tk.valoeghese.fc0.client.render.gui;

import tk.valoeghese.fc0.client.render.model.Textures;
import tk.valoeghese.fc0.client.render.system.gui.GUI;

public class Text extends GUI {
	public Text(String value, float xOffset, float yOffset, float size) {
		super(Textures.FONT_ATLAS);

		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.size = STEP * size;
		this.setText(value.toCharArray());
	}

	private final float xOffset;
	private final float yOffset;
	private final float size;

	public void changeText(String newText) {
		this.destroy();
		this.setText(newText.toCharArray());
	}

	private void setText(char[] text) {
		float x = this.xOffset;
		float y = this.yOffset;

		for (char c : text) {
			if (c == '\n') {
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

		this.generateBuffers();
	}

	private static final float STEP = 0.05f;
}
