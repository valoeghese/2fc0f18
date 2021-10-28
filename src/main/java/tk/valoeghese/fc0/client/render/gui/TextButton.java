package tk.valoeghese.fc0.client.render.gui;

import tk.valoeghese.fc0.client.render.Textures;
import valoeghese.scalpel.gui.GUICollection;

public class TextButton extends GUICollection<GUI> {
	public TextButton(String text, float xOffset, float yOffset, float xSize, float ySize) {
		ButtonRect rect = new ButtonRect(Textures.SIMPLE_BUTTON, xSize, ySize);
		rect.setPosition(xOffset, yOffset);
		this.guis.add(rect);

		float textSize = ySize * 10;
		Text.Moveable textGUI = new Text.Moveable(text,
		xOffset - Text.widthOf(text.toCharArray()) / textSize,
				yOffset,
				textSize);
		this.guis.add(textGUI);
	}
}
