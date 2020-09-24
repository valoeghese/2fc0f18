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
	protected int maxHealth = 100;
	private int health = this.maxHealth;

	public Inventory getInventory() {
		return this.inventory;
	}

	public int getHealth() {
		return this.health;
	}

	public int getMaxHealth() {
		return this.maxHealth;
	}

	public int damage(int amount) {
		return (this.health -= amount);
	}

	public boolean isAlive() {
		return this.health > 0;
	}

	public void setHealth(int health) {
		this.health = Math.min(health, this.maxHealth);
	}

	public void setMaxHealth(int maxHealth) {
		this.maxHealth = maxHealth;
	}

	@Override
	public void hitGround() {
		double aval = -this.velocity.getY();
		int val = (int) (aval * aval * 36);

		if (val > 5) {
			this.damage(val);
		}

		super.hitGround();
	}
}
