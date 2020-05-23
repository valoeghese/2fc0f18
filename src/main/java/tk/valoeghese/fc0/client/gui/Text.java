package tk.valoeghese.fc0.client.gui;

import tk.valoeghese.fc0.client.model.Textures;

public class Text extends GUI {
	public Text(String value, float xOffset, float yOffset, float size) {
		super(Textures.FONT_ATLAS);

		float x = xOffset;
		float y = yOffset;
		size = STEP * size;

		for (char c : value.toCharArray()) {
			if (c == '\n') {
				y -= size;
				continue;
			}

			if (c < 32) {
				throw new RuntimeException("Text cannot contain non-newline characters lower than 32!");
			}

			int cVal = (int) c - 32; // space is at 0
			int u = cVal % 16;
			int v = cVal / 16;

			final float startU = (u / 16.0f);
			final float startV = (v / 16.0f);
			final float endU = startU + 0.0625f;
			final float endV = startV + 0.0625f;

			int tl = this.vertex(x, y + size, startU, endV);
			int bl = this.vertex(x, y, startU, startV);
			int tr = this.vertex(x + size, y + size, endU, endV);
			int br = this.vertex(x + size, y, endU, startV);

			this.tri(tl, bl, br);
			this.tri(tl, tr, br);

			x += size;
		}

		this.generateBuffers();
	}

	private static final float STEP = 0.05f;
}
