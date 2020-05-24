package tk.valoeghese.fc0.client.gui;

import tk.valoeghese.fc0.client.model.Textures;

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
			int u = cVal % 16;
			int v = cVal / 16;

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

			x += 0.63f * this.size;
		}

		this.generateBuffers();
	}

	private static final float STEP = 0.05f;
}
