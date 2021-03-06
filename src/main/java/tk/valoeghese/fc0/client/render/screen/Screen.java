package tk.valoeghese.fc0.client.render.screen;

import tk.valoeghese.fc0.client.Client2fc;
import valoeghese.scalpel.Window;

public abstract class Screen {
	public Screen(Client2fc game) {
		this.game = game;
	}

	protected final Client2fc game;

	public abstract void renderGUI(float lighting);
	public abstract void handleMouseInput(double dx, double dy);
	public abstract void handleKeybinds();
	public abstract void handleEscape(Window window);

	public void onFocus() {
	}
}
