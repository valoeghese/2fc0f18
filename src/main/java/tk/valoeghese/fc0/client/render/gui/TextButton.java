package tk.valoeghese.fc0.client.render.gui;

import tk.valoeghese.fc0.client.render.Textures;
import valoeghese.scalpel.gui.GUICollection;

public class TextButton extends GUICollection<GUI> {
	public TextButton(String text, float xOffset, float yOffset, float xSize, float ySize) {
		this.button = new ButtonRect(Textures.SIMPLE_BUTTON, xSize, ySize);
		this.button.setPosition(xOffset, yOffset);
		this.guis.add(this.button);

		float textSize = ySize * 10;
		Text.Moveable textGUI = new Text.Moveable(text,
		xOffset - Text.widthOf(text.toCharArray()) / textSize,
				yOffset,
				textSize);
		this.guis.add(textGUI);
	}

	private final ButtonRect button;

	public boolean isCursorSelecting(float x, float y) {
		return this.button.isCursorSelecting(x, y);
	}
}
