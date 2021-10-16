package tk.valoeghese.fc0.client.render.gui;

public class ButtonText extends Text.Moveable {
	public ButtonText(String value, float xOffset, float yOffset, float size) {
		super(value, xOffset, yOffset, size);
	}

	public boolean isCursorSelecting(float x, float y) {
		if (!this.vertexArrays.isEmpty()) {
			if (y < this.yOffset + this.size && y > this.y1) {
				if (x > this.xOffset && x < this.x1) {
					return true;
				}
			}
		}

		return false;
	}
}
