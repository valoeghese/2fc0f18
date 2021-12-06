package tk.valoeghese.fc0.client.screen;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.Keybinds;
import tk.valoeghese.fc0.client.render.Textures;
import tk.valoeghese.fc0.client.render.gui.button.Button;
import tk.valoeghese.fc0.client.sound.MusicSettings;
import tk.valoeghese.fc0.world.sound.SoundEffect;
import valoeghese.scalpel.Window;
import valoeghese.scalpel.util.GLUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class MenuScreen extends Screen {
	public MenuScreen(Client2fc game, Screen parentScreen) {
		super(game);
		this.parentScreen = parentScreen;
	}

	protected void addButton(Button button, Runnable callback) {
		this.buttons.put(button, callback);
	}

	private final Map<Button, Runnable> buttons = new HashMap<>();
	protected final Screen parentScreen;

	@Override
	public void onFocus() {
		GLUtils.enableMouse(this.game.getWindowId());
	}

	@Override
	public void handleMouseInput(double dx, double dy) {
	}

	@Override
	public void renderGUI(float lighting) {
		// blend overlay
		GLUtils.enableBlend();
		Textures.DIMMING_OVERLAY.render();
		GLUtils.disableBlend();

		// render all the buttons
		for (Button button : this.buttons.keySet()) {
			button.render();
		}
	}

	@Override
	public void handleKeybinds() {
		if (Keybinds.DESTROY.hasBeenPressed()) {
			float[] mousePositions = this.game.getWindow().getSelectedPositions();

			for (Map.Entry<Button, Runnable> actionableButtons : this.buttons.entrySet()) {
				if (actionableButtons.getKey().isCursorSelecting(mousePositions)) {
					this.game.playSound(SoundEffect.BUTTON_CLICK);
					actionableButtons.getValue().run();
					break;
				}
			}
		}
	}

	@Override
	public void handleEscape(Window window) {
		this.game.switchScreen(this.parentScreen);
	}

	@Override
	public Optional<MusicSettings> getMusic() {
		return GameScreen.GAME_MUSIC;
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}
}
