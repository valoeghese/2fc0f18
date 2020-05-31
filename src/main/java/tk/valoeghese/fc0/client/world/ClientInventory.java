package tk.valoeghese.fc0.client.world;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.gui.Hotbar;
import tk.valoeghese.fc0.world.player.Inventory;
import tk.valoeghese.fc0.world.player.Item;

import javax.annotation.Nullable;

public class ClientInventory extends Inventory {
	public ClientInventory(int size, Client2fc game) {
		super(size);
		this.game = game;
	}

	private final Client2fc game;

	@Nullable
	@Override
	public Item getItemAt(int slot) throws ArrayIndexOutOfBoundsException {
		Item result = super.getItemAt(slot);
		Hotbar hotbar = this.game.getHotbarRenderer();

		if (hotbar != null && !hotbar.updating) {
			hotbar.update(slot, this.game.getWindowAspect());
		}

		return result;
	}

	@Override
	public void putItemAt(int slot, @Nullable Item item) throws ArrayIndexOutOfBoundsException {
		super.putItemAt(slot, item);
		Hotbar hotbar = this.game.getHotbarRenderer();

		if (hotbar != null) {
			hotbar.update(slot, this.game.getWindowAspect());
		}
	}
}
