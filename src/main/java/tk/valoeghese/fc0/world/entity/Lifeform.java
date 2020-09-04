package tk.valoeghese.fc0.world.entity;

import tk.valoeghese.fc0.world.LoadableWorld;
import tk.valoeghese.fc0.world.player.Inventory;

import javax.annotation.Nonnull;

public abstract class Lifeform extends Entity {
	protected Lifeform(float height, Inventory inventory) {
		super(height);

		this.inventory = inventory;

		if (this.inventory == null) {
			throw new NullPointerException("Inventory cannot be null!");
		}
	}

	protected Lifeform(LoadableWorld world, float height, Inventory inventory) {
		super(world, height);

		this.inventory = inventory;

		if (this.inventory == null) {
			throw new NullPointerException("Inventory cannot be null!");
		}
	}

	@Nonnull
	protected final Inventory inventory;

	public Inventory getInventory() {
		return this.inventory;
	}
}
