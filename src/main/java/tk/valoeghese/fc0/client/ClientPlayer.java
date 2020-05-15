package tk.valoeghese.fc0.client;

import org.joml.Vector3f;
import tk.valoeghese.fc0.client.system.Camera;
import tk.valoeghese.fc0.util.*;
import tk.valoeghese.fc0.world.Tile;
import tk.valoeghese.fc0.world.World;

public class ClientPlayer {
	public ClientPlayer(Camera camera, World world) {
		this.pos = new MutablePos(0, 0, 0);
		this.velocity = new MutablePos(0, 0, 0);
		this.camera = camera;
		this.camera.translateScene(new Vector3f(0, -1.8f, 0)); // 2 blocks tall, camera at head
		this.world = world;
		this.move(0, 51.6, 0);
	}

	private final MutablePos pos;
	private final Camera camera;
	private final World world;
	private final MutablePos velocity;
	private boolean falling = false;

	public Camera getCamera() {
		return this.camera;
	}

	public int getX() {
		return new TilePos(this.pos).x;
	}

	public int getZ() {
		return new TilePos(this.pos).z;
	}

	public void addVelocity(double x, double y, double z) {
		this.velocity.offset(x, y, z);
	}

	public void move(Pos pos) {
		this.move(pos.getX(), pos.getY(), pos.getZ());
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
		this.camera.translateScene(new Vector3f((float) -x, (float) -y, (float) -z));
		return true;
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

	public TilePos getTilePos() {
		return new TilePos(this.pos);
	}

	public RaycastResult rayCast(double maxDistance) {
		maxDistance *= maxDistance;
		double yaw = this.getCamera().getYaw() + Math.PI;
		double pitch = this.getCamera().getPitch();
		Pos toUse = this.pos.ofAdded(0, 1.8, 0);

		double dxCalc = -Math.sin(yaw);
		double dyCalc = -Math.tan(pitch);
		double dzCalc = Math.cos(yaw);

		int sx = MathsUtils.sign(dxCalc);
		int sy = MathsUtils.sign(dyCalc);
		int sz = MathsUtils.sign(dzCalc);

		double dx = initialDir(toUse.getX(), sx);
		double dy = initialDir(toUse.getY(), sy);
		double dz = initialDir(toUse.getZ(), sz);

		double epsilonX = -sx * 0.001;
		double epsilonZ = -sz * 0.001;
		double epsilonY = -sy * 0.001;

		final MutablePos result = new MutablePos(0, 0, 0);

		double d;
		Face face = null;

		do {
			if (dx < dz) {
				if (dx < dy) {
					face = sx < 0 ? Face.EAST : Face.WEST;
					dz = -dx / Math.tan(yaw);

					double adxc = Math.abs(dx); // abs dx for calculation
					double adzc = Math.abs(dz);
					double hdist = Math.sqrt(adxc * adxc + adzc * adzc);
					dy = hdist * dyCalc;
				} else {
					face = sy < 0 ? Face.UP : Face.DOWN;
					double hdist = dy / dyCalc;
					dz = hdist * dzCalc;
					dx = hdist * dxCalc;
				}
			} else {
				if (Math.abs(dz) < Math.abs(dy)) {
					face = sz < 0 ? Face.NORTH : Face.SOUTH;
					dx = -(dz * Math.tan(yaw));

					double adxc = Math.abs(dx); // abs dx for calculation
					double adzc = Math.abs(dz);
					double hdist = Math.sqrt(adxc * adxc + adzc * adzc);
					dy = hdist * dyCalc;
				} else {
					face = sy < 0 ? Face.UP : Face.DOWN;
					double hdist = dy / dyCalc;
					dz = hdist * dzCalc;
					dx = hdist * dxCalc;
				}
			}

			result.set(toUse.getX() + dx + epsilonX, toUse.getY() + dy + epsilonY, toUse.getZ() + dz + epsilonZ);
			TilePos tilePos = new TilePos(result);

			if (this.world.isInWorld(tilePos)) {
				if (Tile.BY_ID[this.world.readTile(tilePos)].isOpaque()) {
					break;
				}
			}

			double adx = Math.abs(dx);
			double ady = Math.abs(dy);
			double adz = Math.abs(dz);

			d = adx * adx + ady * ady * adz * adz;
			dx += sx;
			dy += sy;
			dz += sz;
		} while (d < maxDistance);

		return new RaycastResult(result, face);
	}

	private double initialDir(double n, double direction) {
		if (direction == 0) {
			return 0.0;
		} else if (direction > 0) {
			return Math.ceil(n) - n;
		} else {
			return Math.floor(n) - n;
		}
	}

	public void tick() {
		this.velocity.offsetY(-0.03f);
		this.velocity.mul(0.85, 0.9, 0.85);
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
}
