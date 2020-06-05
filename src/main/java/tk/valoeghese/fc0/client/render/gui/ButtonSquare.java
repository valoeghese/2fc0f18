package tk.valoeghese.fc0.client.render.gui;

import tk.valoeghese.fc0.client.Client2fc;

public class ButtonSquare extends MoveableSquare {
	public ButtonSquare(int texture, float size) {
		super(texture, size);
	}

	private float x0;
	private float x1;
	private float y0;
	private float y1;

	@Override
	public void setPosition(float xOffset, float yOffset) {
		super.setPosition(xOffset, yOffset);

		float aspect = 1f / Client2fc.getInstance().getWindowAspect();

		this.x0 = xOffset - aspect * this.size;
		this.y0 = yOffset - 0.06f;
		this.x1 = xOffset + aspect * 0.06f;
		this.y1 = yOffset + 0.06f;
	}

	public boolean isCursorSelecting(float x, float y) {
		if (!this.vertexArrays.isEmpty()) {
			if (y > this.y0 && y < this.y1) {
				if (x > this.x0 && x < this.x1) {
					return true;
				}
			}
		}

		return false;
	}
}
