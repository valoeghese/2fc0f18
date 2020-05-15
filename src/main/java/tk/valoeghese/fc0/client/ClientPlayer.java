package tk.valoeghese.fc0.client;

import org.joml.Vector3f;
import tk.valoeghese.fc0.client.system.Camera;
import tk.valoeghese.fc0.util.MathsUtils;
import tk.valoeghese.fc0.util.MutablePos;
import tk.valoeghese.fc0.util.Pos;
import tk.valoeghese.fc0.util.TilePos;
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

	public Pos rayCast(double maxDistance) {
		maxDistance *= maxDistance;
		double yaw = this.getCamera().getYaw();
		//double pitch = this.getCamera().getPitch(); TODO add vertical
		Pos toUse = this.pos.ofAdded(0, 1.8, 0);

		int sx = MathsUtils.sign(toUse.getX());
		int sz = MathsUtils.sign(toUse.getZ());
		double dx = initialDir(sx, -Math.sin(yaw));
		double dz = initialDir(sz, Math.cos(yaw));

		final MutablePos result = new MutablePos(0, 0, 0);

		double d;

		do {
			if (dx < dz) {
				dz = -dx / Math.tan(yaw);
			} else {
				dx = -(dz * Math.tan(yaw));
			}

			result.set(toUse.getX() + dx, toUse.getY(), toUse.getZ() + dz);
			TilePos tilePos = new TilePos(result);

			if (this.world.isInWorld(tilePos)) {
				if (Tile.BY_ID[this.world.readTile(tilePos)].isOpaque()) {
					break;
				}
			}

			double adx = Math.abs(dx);
			double adz = Math.abs(dz);

			d = adx * adx + adz * adz;
			dx += sx;
			dz += sz;
		} while (d < maxDistance);

		return result;
	}

	private double initialDir(int direction, double n) {
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
