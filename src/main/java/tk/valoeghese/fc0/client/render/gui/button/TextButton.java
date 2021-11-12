package tk.valoeghese.fc0.client.render.gui.button;

import tk.valoeghese.fc0.client.render.Textures;
import tk.valoeghese.fc0.client.render.gui.GUI;
import tk.valoeghese.fc0.client.render.gui.Text;
import valoeghese.scalpel.gui.GUICollection;

public class TextButton extends GUICollection<GUI> implements Button {
	public TextButton(String text, float xOffset, float yOffset, float height, float buttonWidth) {
		this.button = new ButtonRect(Textures.simpleButton, buttonWidth, height);
		this.button.setPosition(xOffset + 0.02f, yOffset + height * 0.25f);
		this.guis.add(this.button);

		this.textSize = height * 15;
		this.textGUI = new Text.Moveable(text,
		xOffset - Text.widthOf(text.toCharArray()) / this.textSize,
				yOffset,
				this.textSize);
		this.guis.add(this.textGUI);

		this.xOffset = xOffset;
	}

	private final float textSize;
	private final float xOffset;
	private final ButtonRect button;
	private final Text.Moveable textGUI;

	@Override
	public boolean isCursorSelecting(float x, float y) {
		return this.button.isCursorSelecting(x, y);
	}

	@Override
	public boolean isCursorSelecting(float[] positions) {
		return this.button.isCursorSelecting(positions[0], positions[1]);
	}

	public void setText(String text) {
		this.textGUI.changeText(text,
				this.xOffset - Text.widthOf(text.toCharArray()) / this.textSize,
				this.textGUI.getYOffset());
	}

	public static float buttonWidth(String text, float baseWidth, float height) {
		float textOffset = Text.widthOf(text.toCharArray()) / (height * 15);
		return baseWidth * textOffset;
	}
}
