package tk.valoeghese.fc0.client.render.gui;

public class ButtonText extends Text.Moveable {
	public ButtonText(String value, float xOffset, float yOffset, float size) {
		super(value, xOffset, yOffset, size);
	}

	public boolean isCursorSelecting(float x, float y, float leniency) {
		if (!this.vertexArrays.isEmpty()) {
			if (y < this.yOffset + this.size + leniency && y > this.y1 - leniency) {
				if (x > this.xOffset - leniency && x < this.x1 + leniency) {
					return true;
				}
			}
		}

		return false;
	}
}
