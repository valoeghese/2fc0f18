package tk.valoeghese.fc0.client.screen;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.Keybinds;
import tk.valoeghese.fc0.client.render.Textures;
import tk.valoeghese.fc0.client.render.gui.ButtonText;
import tk.valoeghese.fc0.client.render.gui.Overlay;
import tk.valoeghese.fc0.client.render.gui.Text;
import tk.valoeghese.fc0.client.sound.MusicSettings;
import tk.valoeghese.fc0.client.world.ClientPlayer;
import tk.valoeghese.fc0.client.world.ClientWorld;
import tk.valoeghese.fc0.util.maths.Pos;
import valoeghese.scalpel.Window;
import valoeghese.scalpel.util.GLUtils;

import java.util.Optional;

import static tk.valoeghese.fc0.client.Client2fc.NEW_TITLE;
import static tk.valoeghese.fc0.client.Client2fc.PI;

public class PauseScreen extends Screen {
	public PauseScreen(Client2fc game) {
		super(game);

		this.overlay = new Overlay(Textures.DIM);
		this.continueOption = new ButtonText(
				"Continue",
				-0.67f*Text.widthOf("Continue".toCharArray()),
				0.3f,
				1.5f);
		this.saveExitOption = new ButtonText(
				"Save and Exit",
				-0.67f*Text.widthOf("Save and Exit".toCharArray()),
				-0.3f,
				1.5f);
	}

	private final Overlay overlay;
	private final ButtonText continueOption;
	private final ButtonText saveExitOption;

	@Override
	public void onFocus() {
		GLUtils.enableMouse(this.game.getWindowId());
	}

	@Override
	public void renderGUI(float lighting) {
		GLUtils.enableBlend();
		this.overlay.render();
		GLUtils.disableBlend();
		this.continueOption.render();
		this.saveExitOption.render();
	}

	@Override
	public void handleMouseInput(double dx, double dy) {
	}

	@Override
	public void handleKeybinds() {
		if (Keybinds.DESTROY.hasBeenPressed()) {
			float[] mousePositions = this.game.getWindow().getSelectedPositions();

			if (this.continueOption.isCursorSelecting(mousePositions[0], mousePositions[1], 0.07f)) {
				this.game.switchScreen(this.game.gameScreen);
			} else if (this.saveExitOption.isCursorSelecting(mousePositions[0], mousePositions[1], 0.07f)) {
				this.game.saveWorld();
				this.game.getWorld().destroy();
				this.game.save = null;

				// Create a new, unsaving live world with the seed the player last used.
				ClientWorld world = new ClientWorld(null, this.game.getWorld().getSeed(), Client2fc.TITLE_WORLD_SIZE);
				this.game.setWorld(world);
				ClientPlayer player = this.game.getPlayer();
				// Start at leave pos
				int x = player.getX();
				int z = player.getZ();
				player.changeWorld(world, this.game.save, new Pos(x, world.getHeight(x, z) + 1.0, z));
				player.getCamera().setPitch(0);
				player.getCamera().setYaw(PI);

				if (NEW_TITLE) {
					player.setNoClip(true);
					player.move(0, 15, 0);
				}

				this.game.sprintFOV(1.0f);
				this.game.switchScreen(this.game.titleScreen);
			}
		}
	}

	@Override
	public void handleEscape(Window window) {
		this.game.switchScreen(this.game.gameScreen);
	}

	@Override
	public Optional<MusicSettings> getMusic() {
		return GameScreen.GAME_MUSIC;
	}
}
