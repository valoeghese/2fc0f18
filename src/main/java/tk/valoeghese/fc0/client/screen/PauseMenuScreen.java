package tk.valoeghese.fc0.client.screen;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.render.gui.button.TextButton;
import tk.valoeghese.fc0.client.world.ClientPlayer;
import tk.valoeghese.fc0.client.world.ClientWorld;
import tk.valoeghese.fc0.util.maths.ChunkPos;
import tk.valoeghese.fc0.util.maths.Pos;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.save.FakeSave;

import static tk.valoeghese.fc0.client.Client2fc.NEW_TITLE;
import static tk.valoeghese.fc0.client.Client2fc.PI;

public class PauseMenuScreen extends MenuScreen {
	public PauseMenuScreen(Client2fc game, Screen parentScreen) {
		super(game, parentScreen);

		this.addButton(new TextButton(
				"Continue",
				0.0f,
				0.3f,
				0.1f,
				BUTTON_WIDTH),
				() -> this.game.switchScreen(this.game.gameScreen));

		this.addButton(new TextButton(
				"Options",
				0.0f,
				0.0f,
				0.1f,
				BUTTON_WIDTH),
				() -> this.game.switchScreen(this.game.optionsScreen));

		this.addButton(new TextButton(
				"Save and Exit",
				0.0f,
				-0.3f,
				0.1f,
				BUTTON_WIDTH),
				this::saveAndExit);
	}

	private void saveAndExit() {
		this.game.saveWorld();
		this.game.getWorld().destroy();
		this.game.save = null;

		// Create a new, unsaving live world with the seed the player last used.
		long seed = this.game.getWorld().getSeed();
		ClientWorld world = new ClientWorld(new FakeSave(seed), seed, Client2fc.TITLE_WORLD_SIZE);
		this.game.setWorld(world);
		ClientPlayer player = this.game.getPlayer();
		// Start at leave pos
		int x = player.getTileX();
		int z = player.getTileZ();
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

	static final float BUTTON_WIDTH = TextButton.buttonWidth("Save and Exit", 2.25f, 0.1f);
}
