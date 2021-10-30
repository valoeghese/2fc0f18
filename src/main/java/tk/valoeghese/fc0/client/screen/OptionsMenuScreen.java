package tk.valoeghese.fc0.client.screen;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.render.gui.button.TextButton;
import tk.valoeghese.fc0.world.GameplayWorld;
import valoeghese.scalpel.Window;

public class OptionsMenuScreen extends MenuScreen {
	public OptionsMenuScreen(Client2fc game, Screen parentScreen) {
		super(game, parentScreen);

		TextButton chunkLoadDist = new TextButton(
				"Chunkload Distance: Normal",
				0.0f,
				0.2f,
				0.1f,
				BUTTON_WIDTH
		);

		this.addButton(chunkLoadDist, () -> {
			this.reChunkLoad = true;
			switch (GameplayWorld.getChunkTickDist()) {
			case 6:
				GameplayWorld.setChunkTickDist(8);
				chunkLoadDist.setText("Chunkload Distance: Normal");
				break;
			case 8:
				GameplayWorld.setChunkTickDist(10);
				chunkLoadDist.setText("Chunkload Distance: Far");
				break;
			case 10:
				GameplayWorld.setChunkTickDist(6);
				chunkLoadDist.setText("Chunkload Distance: Near");
				break;
			}
		});

		this.addButton(new TextButton(
				"Back",
				0.0f,
				-0.2f,
				0.1f,
				BUTTON_WIDTH
		), () -> {
			this.game.switchScreen(this.parentScreen);

			if (this.reChunkLoad) {
				this.game.getWorld().chunkLoad(this.game.getPlayer().getTilePos().toChunkPos());
				this.reChunkLoad = false;
			}
		});
	}

	@Override
	public void handleEscape(Window window) {
		super.handleEscape(window);

		if (this.reChunkLoad) {
			this.game.getWorld().chunkLoad(this.game.getPlayer().getTilePos().toChunkPos());
			this.reChunkLoad = false;
		}
	}

	private boolean reChunkLoad = false;

	static final float BUTTON_WIDTH = TextButton.buttonWidth("Chunkload Distance: Normal", 2.25f, 0.1f);
}
