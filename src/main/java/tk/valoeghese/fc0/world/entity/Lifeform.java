package tk.valoeghese.fc0.world.entity;

import tk.valoeghese.fc0.Game2fc;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.player.Inventory;
import tk.valoeghese.fc0.world.sound.SoundEffect;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nonnull;

public abstract class Lifeform extends Entity {
	protected Lifeform(float height, Inventory inventory) {
		super(height);

		this.inventory = inventory;

		if (this.inventory == null) {
			throw new NullPointerException("Inventory cannot be null!");
		}
	}

	protected Lifeform(GameplayWorld world, float height, Inventory inventory) {
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
	private int lastFallTickOffset; // to prevent double-playings of the step sound for lifeforms falling then walking

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

	@Override
	public boolean move(double x, double y, double z) {
		if ((Game2fc.getInstance().time & 0x7) == this.lastFallTickOffset && !this.falling && !this.isSwimming() && Math.max(Math.abs(x), Math.abs(z)) > 0.04) {
			Tile on = Tile.BY_ID[this.world.readTile(new TilePos(this.pos).down())];

			if (on.isSolid()) {
				Game2fc.getInstance().playSound(null, on.getSounds().getStepSound(), this.getX(), this.getY(), this.getZ(), 0.4f);
			}
		}
		return super.move(x, y, z);
	}

	public int heal(int amount) {
		this.health += amount;

		if (this.health > this.maxHealth) {
			this.health = this.maxHealth;
		}

		return this.health;
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
		// set tick offset to delay next sound
		this.lastFallTickOffset = (int) ((Game2fc.getInstance().time - 1) & 0x7);

		// play sound
		Tile on = Tile.BY_ID[this.world.readTile(new TilePos(this.pos).down())];

		if (on.isSolid()) {
			Game2fc.getInstance().playSound(null, on.getSounds().getStepSound(), this.getX(), this.getY(), this.getZ(), 0.4f);
		}

		// deal damage based on the square of the velocity if they fell (Ek = 1/2 mv^2. Might change to linear since F = dp/dt, p = mv, and I think damage is more based on force.)
		// dt would just be based on the material softness / flexibility and makes stuff more complicated if we actually handle it and this is a block game not a physics engine
		double aval = -this.velocity.getY();
		int val = (int) (aval * aval * 36);

		if (val > 10) {
			Game2fc.getInstance().playSound(null, SoundEffect.HURT, this.getX(), this.getY() + this.height, this.getZ(), 0.5f);
			this.damage(val / 2);
		}

		super.hitGround();
	}
}
