package tk.valoeghese.fc0.client.render.screen;

import org.lwjgl.glfw.GLFW;
import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.Keybinds;
import tk.valoeghese.fc0.client.render.Textures;
import tk.valoeghese.fc0.client.render.gui.CraftingMenu;
import tk.valoeghese.fc0.client.render.gui.Hotbar;
import tk.valoeghese.fc0.client.render.gui.Overlay;
import tk.valoeghese.fc0.client.render.system.Window;
import tk.valoeghese.fc0.client.render.system.gui.GUI;
import tk.valoeghese.fc0.client.render.system.util.GLUtils;
import tk.valoeghese.fc0.util.maths.MathsUtils;
import tk.valoeghese.fc0.world.player.Inventory;
import tk.valoeghese.fc0.world.player.Item;

public class CraftingScreen extends Screen {
	public CraftingScreen(Client2fc game) {
		super(game);

		this.craftingOverlay = new Overlay(Textures.WATER_OVERLAY);
		this.menu = new CraftingMenu(game.getPlayer().getInventory());
		Hotbar.addUpdateSubscriber(this.menu);
	}

	private final GUI craftingOverlay;
	private final CraftingMenu menu;

	@Override
	public void renderGUI(float lighting) {
		if (!this.menu.isFocused()) {
			this.menu.setFocus(true);
		}

		this.craftingOverlay.render();
		this.menu.render();
		this.game.gameScreen.hotbarRenderer.render();
	}

	@Override
	public void handleMouseInput(double dx, double dy) {
	}

	@Override
	public void handleKeybinds() {
		if (Keybinds.INVENTORY.hasBeenPressed()) {
			this.menu.setFocus(false);
			this.game.switchScreen(this.game.gameScreen);
			return;
		}

		if (Keybinds.DESTROY.hasBeenPressed()) {
			Window window = this.game.getWindow();
			double[] xbuf = new double[1];
			double[] ybuf = new double[1];
			GLFW.glfwGetCursorPos(this.game.getWindowId(), xbuf, ybuf);

			float x = MathsUtils.clampMap((float) xbuf[0], 0, window.width, -1.0f, 1.0f);
			float y = MathsUtils.clampMap((float) ybuf[0], window.height, 0, -1.0f, 1.0f);
			Item crafted = this.menu.getItemToCraft(x, y);

			if (crafted != null) {
				Inventory inventory = this.game.getPlayer().getInventory();

				if (inventory.addItem(crafted)) {
					inventory.getSelectedItem().decrement();
				}
			}
		}

		this.game.gameScreen.updateSelected(this.game.getPlayer().getInventory());
	}

	@Override
	public void handleEscape(Window window) {
		this.menu.setFocus(false);
		this.game.gameScreen.handleEscape(window);
	}

	@Override
	public void onFocus() {
		GLUtils.enableMouse(this.game.getWindowId());
	}
}
