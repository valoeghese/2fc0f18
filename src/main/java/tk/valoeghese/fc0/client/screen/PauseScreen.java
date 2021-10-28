package tk.valoeghese.fc0.client.screen;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.Keybinds;
import tk.valoeghese.fc0.client.render.Textures;
import tk.valoeghese.fc0.client.render.gui.TextButton;
import tk.valoeghese.fc0.client.render.gui.Overlay;
import tk.valoeghese.fc0.client.render.gui.Text;
import tk.valoeghese.fc0.client.sound.MusicSettings;
import tk.valoeghese.fc0.client.world.ClientPlayer;
import tk.valoeghese.fc0.client.world.ClientWorld;
import tk.valoeghese.fc0.util.maths.ChunkPos;
import tk.valoeghese.fc0.util.maths.Pos;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.save.FakeSave;
import valoeghese.scalpel.Window;
import valoeghese.scalpel.util.GLUtils;

import java.util.Optional;

import static tk.valoeghese.fc0.client.Client2fc.NEW_TITLE;
import static tk.valoeghese.fc0.client.Client2fc.PI;

public class PauseScreen extends Screen {
	public PauseScreen(Client2fc game) {
		super(game);

		this.overlay = new Overlay(Textures.DIM);
		this.continueOption = new TextButton(
				"Continue",
				0.0f,
				0.3f,
				0.5f,
				0.15f);
		this.saveExitOption = new TextButton(
				"Save and Exit",
				0.0f,
				-0.3f,
				0.5f,
				0.15f);
	}

	private final Overlay overlay;
	private final TextButton continueOption;
	private final TextButton saveExitOption;

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

			if (this.continueOption.isCursorSelecting(mousePositions[0], mousePositions[1])) {
				this.game.switchScreen(this.game.gameScreen);
			} else if (this.saveExitOption.isCursorSelecting(mousePositions[0], mousePositions[1])) {
				this.game.saveWorld();
				this.game.getWorld().destroy();
				this.game.save = null;

				// Create a new, unsaving live world with the seed the player last used.
				long seed = this.game.getWorld().getSeed();
				ClientWorld world = new ClientWorld(new FakeSave(seed), seed, Client2fc.TITLE_WORLD_SIZE);
				this.game.setWorld(world);
				ClientPlayer player = this.game.getPlayer();
				// Start at leave pos
				int x = player.getX();
				int z = player.getZ();
				player.getCamera().setPitch(0);
				player.getCamera().setYaw(PI);

				if (NEW_TITLE) {
					player.setNoClip(true);
				}

				this.game.sprintFOV(1.0f);
				this.game.switchScreen(this.game.titleScreen);

				world.chunkLoad(new ChunkPos(x >> 4, z >> 4));
				world.scheduleForChunk(GameplayWorld.key(x >> 4, z >> 4),
						c -> player.changeWorld(world, this.game.save, new Pos(x, world.getHeight(x, z) + (NEW_TITLE ? 16.0 : 1.0), z)),
						"changeToTitleWorld");
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
