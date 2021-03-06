package tk.valoeghese.fc0.client.render.screen;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.Keybinds;
import tk.valoeghese.fc0.client.render.Textures;
import tk.valoeghese.fc0.client.render.gui.Overlay;
import tk.valoeghese.fc0.client.render.gui.Text;
import tk.valoeghese.fc0.client.world.ClientPlayer;
import valoeghese.scalpel.Window;
import valoeghese.scalpel.util.GLUtils;

public class YouDiedScreen extends Screen {
	public YouDiedScreen(Client2fc game) {
		super(game);

		this.overlay = new Overlay(Textures.DEATH_OVERLAY);
		this.title = new Text(YOU_DIED, -1.5f*Text.widthOf(YOU_DIED.toCharArray()), 0.7f, 2.5f);
		this.subtitle = new Text(TUTORIAL, -Text.widthOf(TUTORIAL.toCharArray()), 0.5f, 1.0f);
	}

	private final Overlay overlay;
	private final Text title;
	private final Text subtitle;

	@Override
	public void renderGUI(float lighting) {
		GLUtils.enableBlend();
		this.overlay.render();
		GLUtils.disableBlend();
		this.title.render();
		this.subtitle.render();
	}

	@Override
	public void handleMouseInput(double dx, double dy) {
	}

	@Override
	public void handleKeybinds() {
		if (Keybinds.DESTROY.hasBeenPressed()) {
			ClientPlayer player = this.game.getPlayer();
			player.setHealth(player.getMaxHealth());
			player.setPos(this.game.spawnLoc);
			this.game.switchScreen(this.game.gameScreen);
			this.game.activateLoadScreen();
		}
	}

	@Override
	public void handleEscape(Window window) {
		this.game.gameScreen.handleEscape(window);
	}

	public static final String YOU_DIED = "You Absolute Buffoon";
	public static final String TUTORIAL = "Click to respawn";
}
