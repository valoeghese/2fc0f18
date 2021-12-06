package tk.valoeghese.fc0.world.entity;

import tk.valoeghese.fc0.client.render.entity.EntityRenderer;
import tk.valoeghese.fc0.util.maths.MathsUtils;
import tk.valoeghese.fc0.util.maths.MutablePos;
import tk.valoeghese.fc0.util.maths.Pos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.LoadableWorld;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nullable;

public abstract class Entity {
	protected Entity(float height) {
		this.pos = new MutablePos(0 ,0, 0);
		this.nextPos = new MutablePos(0, 0, 0);
		this.velocity = new MutablePos(0, 0, 0);
		this.height = height;
	}

	protected Entity(GameplayWorld world, float height) {
		this(height);
		this.world = world;
	}

	protected final MutablePos pos;
	protected final MutablePos nextPos;
	protected final MutablePos velocity;
	protected final float height;
	protected GameplayWorld world;
	protected boolean noClip = false;
	protected boolean falling = false;
	protected double friction = 0.75;

	public void tick() {
		this.pos.set(this.nextPos);

		TilePos below = this.getTilePos().down();

		if (!this.noClip && this.world.isInWorld(below)) {
			byte tile = this.world.readTile(below);

			if (tile != Tile.AIR.id) {
				this.friction = 0.75;
				this.friction /= Tile.BY_ID[tile].getFrictionConstant();
				this.friction = Math.min(1.0, this.friction);
			}
		}

		if (!this.noClip) { // -0.01, -0.02 originally
			this.velocity.offsetY(this.isSwimming() ? -0.04f : -0.08f);
		}

		this.velocity.mul(this.friction, this.noClip ? 0.96 : (this.isSwimming() && this.velocity.getY() < 0 ? 0.75 : 0.98), this.friction); // swimming slowfall. also noclip is special bunny
		this.move(this.velocity.getX(), 0.0, 0.0);
		this.move(0.0, 0.0, this.velocity.getZ());

		if (Math.abs(this.velocity.getY()) > 0.03) {
			this.falling = true;
		}

		if (!this.move(0.0, this.velocity.getY(), 0.0)) {
			this.hitGround();
		}
	}

	public void forceMove(double x, double y, double z) {
		this.nextPos.offset(x, y, z);
	}

	public boolean move(double x, double y, double z) {
		if (this.world.getChunk(this.getTilePos().toChunkPos()) == null) {
			return false;
		} else {
			Pos next = this.nextPos.ofAdded(x, y, z);
			Pos test = this.nextPos.ofAdded(x + MathsUtils.sign(x) * 0.04, y, z + MathsUtils.sign(z) * 0.04);

			if (!this.noClip) {
				TilePos tilePos = new TilePos(test);

				if (this.world.isInWorld(tilePos)) {
					if (Tile.BY_ID[this.world.readTile(tilePos)].isSolid()) {
						return false;
					}
				}

				// TODO fix problem where player's face gets slammed into block and can move along it
				tilePos = new TilePos(test.ofAdded(0, this.height, 0));

				if (this.world.isInWorld(tilePos)) {
					if (Tile.BY_ID[this.world.readTile(tilePos)].isSolid()) {
						return false;
					}
				}
			}

			this.nextPos.set(next);
			return true;
		}
	}

	// setters and adders, etc.

	public void setPos(Pos pos) {
		this.pos.set(pos);
		this.nextPos.set(pos);
	}

	public void addVelocity(double x, double y, double z) {
		this.velocity.offset(x, y, z);
	}

	// checkers
	public boolean isNoClip() {
		return this.noClip;
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

	public boolean isSwimming() {
		TilePos pos = this.getTilePos();

		if (this.world.isInWorld(pos)) {
			return this.world.readTile(pos) == Tile.WATER.id;
		} else {
			return false;
		}
	}

	public boolean isUnderwater() {
		TilePos pos = new TilePos(this.pos.ofAdded(0, this.height, 0));

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

	public LoadableWorld getWorld() {
		return this.world;
	}

	public int getTileX() {
		return MathsUtils.floor(this.pos.getX());
	}

	public int getTileZ() {
		return MathsUtils.floor(this.pos.getZ());
	}

	public double getX() {
		return this.pos.getX();
	}

	public double getY() {
		return this.pos.getY();
	}

	public double getZ() {
		return this.pos.getZ();
	}

	public Pos getPos() {
		return new Pos(this.pos);
	}

	public Pos getVelocity() {
		return new Pos(this.velocity);
	}

	@Nullable
	public EntityRenderer getRenderer() {
		return null;
	}

	public void hitGround() {
		this.falling = false;
		this.velocity.setY(0.0);
	}
}
