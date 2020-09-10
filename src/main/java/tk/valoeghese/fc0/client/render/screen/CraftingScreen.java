package tk.valoeghese.fc0.client.render.screen;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.Keybinds;
import tk.valoeghese.fc0.client.render.Textures;
import tk.valoeghese.fc0.client.render.gui.Overlay;
import tk.valoeghese.fc0.client.render.gui.collection.CraftingMenu;
import tk.valoeghese.fc0.client.render.gui.collection.Hotbar;
import valoeghese.scalpel.Window;
import tk.valoeghese.fc0.client.render.gui.GUI;
import valoeghese.scalpel.util.GLUtils;
import tk.valoeghese.fc0.world.player.Inventory;
import tk.valoeghese.fc0.world.player.Item;

public class CraftingScreen extends Screen {
	public CraftingScreen(Client2fc game) {
		super(game);

		this.craftingOverlay = new Overlay(Textures.CRAFTING);
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

		GLUtils.enableBlend();
		this.craftingOverlay.render();
		GLUtils.disableBlend();

		this.menu.render();
		this.game.gameScreen.hotbarRenderer.render();
	}

	@Override
	public void handleMouseInput(double dx, double dy) {
	}

	private void closeInventory() {
		this.menu.setFocus(false);
		this.game.switchScreen(this.game.gameScreen);
	}

	@Override
	public void handleKeybinds() {
		if (Keybinds.INVENTORY.hasBeenPressed()) {
			this.closeInventory();
			return;
		}

		if (Keybinds.DESTROY.hasBeenPressed()) {
			Window window = this.game.getWindow();
			float[] positions = window.getSelectedPositions();
			Item crafted = this.menu.getItemToCraft(positions[0], positions[1]);

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
		this.closeInventory();
	}

	@Override
	public void onFocus() {
		GLUtils.enableMouse(this.game.getWindowId());
	}
}
