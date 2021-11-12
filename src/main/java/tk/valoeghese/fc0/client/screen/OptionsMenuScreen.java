package tk.valoeghese.fc0.client.screen;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.render.Textures;
import tk.valoeghese.fc0.client.render.gui.button.TextButton;
import tk.valoeghese.fc0.world.GameplayWorld;
import valoeghese.scalpel.Window;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class OptionsMenuScreen extends MenuScreen {
	public OptionsMenuScreen(Client2fc game, Screen parentScreen, Properties preferred) {
		super(game, parentScreen);

		this.preferred = preferred;

		// Load Chunkload Distance Property
		GameplayWorld.setChunkloadDistance(Integer.parseInt(this.preferred.getProperty("chunkloadDistance", "8")));

		if (this.preferred.contains("chunkloadDistance")) {
			System.out.println("Setting chunkload distance from preferences " + GameplayWorld.getChunkloadDistance());
		}

		TextButton chunkLoadDist = new TextButton(
				"Chunkload Distance: " + switch (GameplayWorld.getChunkloadDistance()) {
					case 6 -> "Near";
					case 8 -> "Normal";
					case 10 -> "Far";
					case 12 -> "Insane";
					default -> "Custom";
				},
				0.0f,
				0.3f,
				0.1f,
				BUTTON_WIDTH
		);

		this.addButton(chunkLoadDist, () -> {
			this.reChunkLoad = true;
			this.dirty = true;
			switch (GameplayWorld.getChunkloadDistance()) {
			case 6:
			default:
				GameplayWorld.setChunkloadDistance(8);
				this.preferred.setProperty("chunkloadDistance", "8");
				chunkLoadDist.setText("Chunkload Distance: Normal");
				break;
			case 8:
				GameplayWorld.setChunkloadDistance(10);
				this.preferred.setProperty("chunkloadDistance", "10");
				chunkLoadDist.setText("Chunkload Distance: Far");
				break;
			case 10:
				GameplayWorld.setChunkloadDistance(12);
				this.preferred.setProperty("chunkloadDistance", "12");
				chunkLoadDist.setText("Chunkload Distance: Insane");
				break;
			case 12:
				GameplayWorld.setChunkloadDistance(6);
				this.preferred.setProperty("chunkloadDistance", "6");
				chunkLoadDist.setText("Chunkload Distance: Near");
				break;
			}
		});

		TextButton mipmapButton = new TextButton(
				"Mipmapping " + (Textures.isMipmappingMipmappables() ? "On" : "Off"),
				0.0f,
				0.0f,
				0.1f,
				BUTTON_WIDTH
		);

		this.addButton(mipmapButton, () -> {
			mipmapButton.setText("Mipmapping " + (Textures.toggleMipmapping() ? "On" : "Off"));
			this.dirty = true;
		});

		this.addButton(new TextButton(
				"Back",
				0.0f,
				-0.3f,
				0.1f,
				BUTTON_WIDTH
		), () -> this.handleEscape(this.game.getWindow()));
	}

	@Override
	public void handleEscape(Window window) {
		super.handleEscape(window);

		if (this.reChunkLoad) {
			this.game.getWorld().chunkLoad(this.game.getPlayer().getTilePos().toChunkPos());
			this.reChunkLoad = false;
		}

		if (this.dirty) {
			// will already exist due to constructed on the client
			// server isn't even implemented it doesn't count
			try (FileWriter writer = new FileWriter("preferences.txt")) {
				this.preferred.store(writer, "user preferences");
			} catch (IOException e) {
				System.err.println("Couldn't save preferences due to error!");
				e.printStackTrace();
			}
		}
	}

	private boolean reChunkLoad = false;
	private boolean dirty;
	private final Properties preferred;

	static final float BUTTON_WIDTH = TextButton.buttonWidth("Chunkload Distance: Normal", 2.25f, 0.1f);
}
