package tk.valoeghese.fc0.world.player;

import tk.valoeghese.fc0.util.maths.MathsUtils;
import tk.valoeghese.fc0.util.maths.MutablePos;
import tk.valoeghese.fc0.util.maths.Pos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.LoadableWorld;
import tk.valoeghese.fc0.world.save.Save;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.IntFunction;

public class Player {
	public Player(boolean dev, IntFunction<Inventory> inventoryConstructor) {
		this.pos = new MutablePos(0, 0, 0);
		this.velocity = new MutablePos(0, 0, 0);
		this.dev = dev;
		this.inventory = inventoryConstructor.apply(10);

		if (this.inventory == null) {
			throw new NullPointerException("Inventory cannot be null!");
		}
	}

	protected final MutablePos pos;
	protected LoadableWorld world;
	protected final MutablePos velocity;
	protected boolean falling = false;
	@Nullable
	public Chunk chunk = null;
	public long lockSwim = 0;
	public final boolean dev;
	@Nonnull
	private final Inventory inventory;
	private double friction = 0.85;
	private boolean noClip = false;

	private void addDevItems() {
		this.inventory.putItemAt(0, new Item(Tile.STONE));
		this.inventory.putItemAt(1, new Item(Tile.GRASS));
		this.inventory.putItemAt(2, new Item(Tile.LOG));
		this.inventory.putItemAt(3, new Item(Tile.LEAVES));
		this.inventory.putItemAt(4, new Item(Tile.SAND));
		this.inventory.putItemAt(5, new Item(Tile.DAISY));
		this.inventory.putItemAt(6, new Item(Tile.TALLGRASS));
		this.inventory.putItemAt(7, new Item(Tile.BRICKS));
		this.inventory.putItemAt(8, new Item(Tile.STONE_BRICKS));
		this.inventory.putItemAt(9, new Item(Tile.ICE));
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

	public boolean move(double x, double y, double z) {
		Pos next = this.pos.ofAdded(x, y, z);
		Pos test = this.pos.ofAdded(x + MathsUtils.sign(x) * 0.03, y, z + MathsUtils.sign(z) * 0.03);

		if (!this.noClip) {
			TilePos tilePos = new TilePos(test);

			if (this.world.isInWorld(tilePos)) {
				if (Tile.BY_ID[this.world.readTile(tilePos)].isSolid()) {
					return false;
				}
			}

			tilePos = new TilePos(test.ofAdded(0, 1.8, 0));

			if (this.world.isInWorld(tilePos)) {
				if (Tile.BY_ID[this.world.readTile(tilePos)].isSolid()) {
					return false;
				}
			}
		}

		this.pos.set(next);
		this.world.updateChunkOf(this);
		return true;
	}

	public void tick() {
		TilePos below = this.getTilePos().down();

		if (!this.noClip && this.world.isInWorld(below)) {
			byte tile = this.world.readTile(below);

			if (tile != Tile.AIR.id) {
				this.friction = 0.85;
				this.friction /= Tile.BY_ID[tile].getFrictionConstant();
				this.friction = Math.min(1.0, this.friction);
			}
		}

		if (!this.noClip) {
			this.velocity.offsetY(this.isSwimming() ? -0.01f : -0.025f);
		}

		this.velocity.mul(this.friction, 0.96, this.friction);
		this.move(this.velocity.getX(), 0.0, 0.0);
		this.move(0.0, 0.0, this.velocity.getZ());

		if (Math.abs(this.velocity.getY()) > 0.03) {
			this.falling = true;
		}

		if (!this.move(0.0, this.velocity.getY(), 0.0)) {
			this.falling = false;
			this.velocity.setY(0.0);
		}
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

	public boolean isSwimming() {
		TilePos pos = this.getTilePos();

		if (this.world.isInWorld(pos)) {
			return this.world.readTile(pos) == Tile.WATER.id;
		} else {
			return false;
		}
	}

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

	public boolean isUnderwater() {
		TilePos pos = new TilePos(this.pos.ofAdded(0, 1.8, 0));

		if (this.world.isInWorld(pos)) {
			return this.world.readTile(pos) == Tile.WATER.id;
		} else {
			return false;
		}
	}

	// getters

	public TilePos getTilePos() {
		return new TilePos(this.pos);
	}

	public float getHorizontalSlowness() {
		return 52.0f;
	}

	public double getJumpStrength() {
		return 11.0 / 30.0;
	}

	public int getX() {
		return new TilePos(this.pos).x;
	}

	public int getZ() {
		return new TilePos(this.pos).z;
	}

	public Pos getPos() {
		return new Pos(this.pos);
	}

	public Inventory getInventory() {
		return this.inventory;
	}
}
