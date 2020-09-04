package tk.valoeghese.fc0.world.player;

import tk.valoeghese.fc0.util.maths.Pos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.LoadableWorld;
import tk.valoeghese.fc0.world.entity.Entity;
import tk.valoeghese.fc0.world.save.Save;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.IntFunction;

public class Player extends Entity {
	public Player(boolean dev, IntFunction<Inventory> inventoryConstructor) {
		super(1.8f);
		this.dev = dev;
		this.inventory = inventoryConstructor.apply(10);

		if (this.inventory == null) {
			throw new NullPointerException("Inventory cannot be null!");
		}
	}

	@Nullable
	public Chunk chunk = null;
	public long lockSwim = 0;
	public boolean dev;
	@Nonnull
	private final Inventory inventory;

	public void addDevItems() {
		this.inventory.putItemAt(0, new Item(Tile.STONE));
		this.inventory.putItemAt(1, new Item(Tile.GRASS));
		this.inventory.putItemAt(2, new Item(Tile.LOG));
		this.inventory.putItemAt(3, new Item(Tile.LEAVES));
		this.inventory.putItemAt(4, new Item(Tile.SAND));
		this.inventory.putItemAt(5, new Item(Tile.DAISY));
		this.inventory.putItemAt(6, new Item(Tile.TALLGRASS));
		this.inventory.putItemAt(7, new Item(Tile.BRICKS));
		this.inventory.putItemAt(8, new Item(Tile.TORCH));
		this.inventory.putItemAt(9, new Item(Tile.ICE));
	}

	public void toggleDev() {
		this.dev = !this.dev;
	}

	public void changeWorld(LoadableWorld world, @Nullable Save save) {
		this.world = world;
		this.setPos(Pos.ZERO);

		if (save == null) {
			this.move(0, world.getHeight(0, 0) + 1f, 0);
		} else {
			int x = (world.getSpawnPos().x << 4) + 8;
			int z = (world.getSpawnPos().z << 4) + 8;
			this.move(x, world.getHeight(x, z) + 1f, z);
		}

		this.world.chunkLoad(this.getTilePos().toChunkPos());
		this.loadNullableInventory(save);
	}

	public void changeWorld(LoadableWorld world, Pos movePos, @Nullable Save save) {
		this.world = world;
		this.setPos(movePos);
		this.world.chunkLoad(this.getTilePos().toChunkPos());
		this.loadNullableInventory(save);
	}

	private void loadNullableInventory(@Nullable Save save) {
		if (save != null) {
			if (save.loadedInventory != null) {
				this.loadInventory(save);
				return;
			}
		}

		this.inventory.reset();

		if (this.dev) {
			this.addDevItems();
		}
	}

	private void loadInventory(@Nonnull Save save) {
		for (int i = 0; i < save.loadedInventory.length; ++i) {
			this.inventory.putItemAt(i, save.loadedInventory[i]);
		}
	}

	@Override
	public boolean move(double x, double y, double z) {
		if (super.move(x, y, z)) {
			this.world.updateChunkOf(this);
			return true;
		}

		return false;
	}

	public final void move(Pos pos) {
		this.move(pos.getX(), pos.getY(), pos.getZ());
	}

	// setters and adders etc.

	public void addVelocity(double x, double y, double z) {
		this.velocity.offset(x, y, z);
	}

	public void setPos(Pos pos) {
		this.pos.set(pos);
		this.world.updateChunkOf(this);
	}

	// checkers

	public boolean isOnGround() {
		if (this.falling) {
			return false;
		}

		TilePos check = new TilePos(this.pos).down();

		if (this.world.isInWorld(check)) {
			if (Tile.BY_ID[this.world.readTile(check)].isSolid()) {
				return true;
			}
		}

		return false;
	}

	public void setNoClip(boolean noClip) {
		this.noClip = noClip;
	}

	public boolean isNoClip() {
		return this.noClip;
	}

	// getters

	public float getHorizontalSlowness() {
		return 52.0f;
	}

	public double getJumpStrength() {
		return 11.0 / 30.0;
	}

	public Inventory getInventory() {
		return this.inventory;
	}
}
