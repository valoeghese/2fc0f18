package tk.valoeghese.fc0.world.entity;

import tk.valoeghese.fc0.world.player.Inventory;

import javax.annotation.Nonnull;
import java.util.function.IntFunction;

public abstract class Lifeform extends Entity {
	protected Lifeform(float height, Inventory inventory) {
		super(height);

		this.inventory = inventory;

		if (this.inventory == null) {
			throw new NullPointerException("Inventory cannot be null!");
		}
	}

	@Nonnull
	protected final Inventory inventory;
}
