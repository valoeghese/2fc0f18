package tk.valoeghese.fc0.client;

import org.joml.Vector3f;
import tk.valoeghese.fc0.client.system.Camera;
import tk.valoeghese.fc0.util.MutablePos;
import tk.valoeghese.fc0.util.Pos;
import tk.valoeghese.fc0.util.TilePos;
import tk.valoeghese.fc0.world.Tile;
import tk.valoeghese.fc0.world.World;

public class ClientPlayer {
	public ClientPlayer(Camera camera, World world) {
		this.pos = new MutablePos(0, 0, 0);
		this.camera = camera;
		this.camera.translateScene(new Vector3f(0, -1.8f, 0)); // 2 blocks tall, camera at head
		this.world = world;
		this.move(0, 51.6, 0);
	}

	private final MutablePos pos;
	private final Camera camera;
	private final World world;

	public Camera getCamera() {
		return this.camera;
	}

	public int getX() {
		return new TilePos(this.pos).x;
	}

	public int getZ() {
		return new TilePos(this.pos).z;
	}

	public void move(double x, double y, double z) {
		Pos next = this.pos.ofAdded(x, y, z);
		TilePos tilePos = new TilePos(next);

		if (this.world.isInWorld(tilePos)) {
			if (Tile.BY_ID[this.world.readTile(tilePos)].isOpaque()) {
				return;
			}
		}

		this.pos.set(next);
		this.camera.translateScene(new Vector3f((float) -x, (float) -y, (float) -z));
	}

	public void tick() {
	}
}
