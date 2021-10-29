package tk.valoeghese.fc0.client.screen;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.sound.MusicSettings;
import valoeghese.scalpel.Window;

import javax.annotation.Nullable;
import java.util.Optional;

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

	public Optional<MusicSettings> getMusic() {
		return Optional.empty();
	}

	public boolean isPauseScreen() {
		return false;
	}
}
