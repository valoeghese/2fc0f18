package tk.valoeghese.fc0.client.screen;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.render.Textures;
import tk.valoeghese.fc0.client.render.gui.Overlay;
import valoeghese.scalpel.Window;

public class PauseScreen extends Screen {
	public PauseScreen(Client2fc game) {
		super(game);

		this.overlay = new Overlay(Textures.DIM);
	}

	private final Overlay overlay;

	@Override
	public void renderGUI(float lighting) {

	}

	@Override
	public void handleMouseInput(double dx, double dy) {

	}

	@Override
	public void handleKeybinds() {

	}

	@Override
	public void handleEscape(Window window) {

	}
}
