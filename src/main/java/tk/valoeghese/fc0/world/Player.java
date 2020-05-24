package tk.valoeghese.fc0.world;

import tk.valoeghese.fc0.util.maths.MutablePos;
import tk.valoeghese.fc0.util.maths.Pos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nullable;

public class Player {
	public Player() {
		this.pos = new MutablePos(0, 0, 0);
		this.velocity = new MutablePos(0, 0, 0);
	}

	protected final MutablePos pos;
	protected World world;
	protected final MutablePos velocity;
	protected boolean falling = false;
	@Nullable
	public Chunk chunk = null;
	public long lockSwim = 0;
	public Tile selectedTile = Tile.STONE;

	public void changeWorld(World world) {
		this.world = world;
		this.setPos(Pos.ZERO);
		this.move(0, world.getHeight(0, 0) + 0.5f, 0);
	}

	public boolean move(double x, double y, double z) {
		Pos next = this.pos.ofAdded(x, y, z);
		TilePos tilePos = new TilePos(next);

		if (this.world.isInWorld(tilePos)) {
			if (Tile.BY_ID[this.world.readTile(tilePos)].isOpaque()) {
				return false;
			}
		}

		tilePos = tilePos.up();

		if (this.world.isInWorld(tilePos)) {
			if (Tile.BY_ID[this.world.readTile(tilePos)].isOpaque()) {
				return false;
			}
		}

		this.pos.set(next);
		return true;
	}

	public void tick() {
		this.velocity.offsetY(this.isSwimming() ? -0.01f : -0.025f);
		this.velocity.mul(0.85, 0.96, 0.85);
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
			if (Tile.BY_ID[this.world.readTile(check)].isOpaque()) {
				return true;
			}
		}

		return false;
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
		return 40.0f;
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
}
