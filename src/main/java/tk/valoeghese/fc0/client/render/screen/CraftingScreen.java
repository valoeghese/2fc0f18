package tk.valoeghese.fc0.client.render.screen;

import org.lwjgl.glfw.GLFW;
import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.Keybinds;
import tk.valoeghese.fc0.client.render.gui.Overlay;
import tk.valoeghese.fc0.client.render.model.Shaders;
import tk.valoeghese.fc0.client.render.model.Textures;
import tk.valoeghese.fc0.client.render.system.Window;
import tk.valoeghese.fc0.client.render.system.gui.GUI;

public class CraftingScreen extends Screen {
	public CraftingScreen(Client2fc game) {
		super(game);

		this.craftingOverlay = new Overlay(Textures.WATER_OVERLAY);
	}

	private final GUI craftingOverlay;

	@Override
	public void renderGUI(float lighting) {
		this.craftingOverlay.render();

		Shaders.gui.uniformFloat("lighting", (lighting - 1.0f) * 0.5f + 1.0f);
		this.game.gameScreen.hotbarRenderer.render();
		Shaders.gui.uniformFloat("lighting", 1.0f);
	}

	@Override
	public void handleMouseInput(double dx, double dy) {
	}

	@Override
	public void handleKeybinds() {
		if (Keybinds.INVENTORY.hasBeenPressed()) {
			this.game.switchScreen(this.game.gameScreen);
			return;
		}

		if (Keybinds.DESTROY.hasBeenPressed()) {
			double[] x = new double[1];
			double[] y = new double[1];
			GLFW.glfwGetCursorPos(this.game.getWindowId(), x, y);

			System.out.println(x + ", " + y);
		}
		this.game.gameScreen.updateSelected(this.game.getPlayer().getInventory());
	}

	@Override
	public void handleEscape(Window window) {
		this.game.gameScreen.handleEscape(window);
	}
}
