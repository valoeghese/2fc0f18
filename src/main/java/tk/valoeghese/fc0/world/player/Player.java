package tk.valoeghese.fc0.world.player;

import tk.valoeghese.fc0.Game2fc;
import tk.valoeghese.fc0.util.maths.ChunkPos;
import tk.valoeghese.fc0.util.maths.Pos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.chunk.Chunk;
import tk.valoeghese.fc0.world.LoadableWorld;
import tk.valoeghese.fc0.world.entity.Lifeform;
import tk.valoeghese.fc0.world.save.FakeSave;
import tk.valoeghese.fc0.world.save.Save;
import tk.valoeghese.fc0.world.save.SaveLike;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntFunction;

public class Player extends Lifeform {
	public Player(boolean dev, IntFunction<Inventory> inventoryConstructor) {
		super(1.8f, inventoryConstructor.apply(10));
		this.dev = dev;
	}

	@Nullable
	public Chunk chunk = null;
	/**
	 * Only access from GameplayWorld#updateChunkOf or when it's set to null in changeWorld.
	 */
	public ChunkPos lastChunkloadChunk;
	public long lockSwim = 0;
	public boolean dev;

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

	public void changeWorld(GameplayWorld world, SaveLike save) {
		// FIXME when player spawns it it has no damn clue about anything in the world. This probably affects all player teleportation.
		this.world = world;
		this.lastChunkloadChunk = null;
		this.chunk = null;
		this.setPos(Pos.ZERO);
		this.world.chunkLoad(this.getTilePos().toChunkPos());

		if (save instanceof Save) {
			// TODO maybe find a better way of doing this
			ChunkPos spawnPos = world.getSpawnPos();

			this.world.scheduleForChunk(GameplayWorld.key(spawnPos.x, spawnPos.z), c -> {
				int x = (spawnPos.x << 4) + 8;
				int z = (world.getSpawnPos().z << 4) + 8;
				this.forceMove(x, world.getHeight(x, z) + 1.0, z);
			}, "moveToSpawnPos");
		} else {
			this.world.scheduleForChunk(GameplayWorld.key(0, 0), c -> this.forceMove(0, world.getHeight(0, 0) + 1.0, 0), "moveToFakeSaveSpawnPos");
		}

		this.loadNullableInventory(save);
	}

	public void changeWorld(GameplayWorld world, SaveLike save, Pos movePos) {
		this.world = world;
		this.lastChunkloadChunk = null;
		this.chunk = null;
		this.setPos(movePos);
		this.loadNullableInventory(save);
	}

	private void loadNullableInventory(SaveLike saveLike) {
		if (saveLike instanceof Save save) {
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

	@Override
	public void forceMove(double x, double y, double z) {
		super.forceMove(x, y, z);
		this.world.updateChunkOf(this);
	}

	public final void forceMove(Pos pos) {
		this.forceMove(pos.getX(), pos.getY(), pos.getZ());
	}

	public final void move(Pos pos) {
		this.move(pos.getX(), pos.getY(), pos.getZ());
	}

	// setters and adders etc.

	@Override
	public int damage(int amount) {
		if (this.dev) {
			return this.getHealth();
		}

		return super.damage(amount);
	}

	@Override
	public void setPos(Pos pos) {
		super.setPos(pos);
		this.world.updateChunkOf(this);
	}

	public TilePos getNextTilePos() {
		return new TilePos(this.nextPos);
	}

	public void setNoClip(boolean noClip) {
		this.noClip = noClip;
	}

	// getters

	public float getHorizontalSlowness() {
		return 14.0f;
	}

	public double getJumpStrength() {
		return 14.0 / 30.0;
	}

	public double getUpwardsSwimStrength() {
		return 50.0 / 30.0;
	}
}
