package tk.valoeghese.fc0.client.screen;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.render.gui.button.TextButton;

public class OptionsMenuScreen extends MenuScreen {
	public OptionsMenuScreen(Client2fc game, Screen parentScreen) {
		super(game, parentScreen);

		this.addButton(new TextButton(
				"Back",
				0.0f,
				0.3f,
				0.1f,
				PauseMenuScreen.BUTTON_WIDTH
		), () -> this.game.switchScreen(this.parentScreen));
	}
}
