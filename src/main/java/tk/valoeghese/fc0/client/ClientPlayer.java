package tk.valoeghese.fc0.client;

import org.joml.Vector3f;
import tk.valoeghese.fc0.client.system.Camera;
import tk.valoeghese.fc0.util.MutablePos;
import tk.valoeghese.fc0.util.Pos;
import tk.valoeghese.fc0.util.TilePos;
import tk.valoeghese.fc0.world.Tile;
import tk.valoeghese.fc0.world.TileAccess;

public class ClientPlayer {
	public ClientPlayer(Camera camera, TileAccess world) {
		this.pos = new MutablePos(0, 51, 0);
		this.camera = camera;
		this.world = world;
	}

	private final MutablePos pos;
	private final Camera camera;
	private final TileAccess world;

	public Camera getCamera() {
		return this.camera;
	}

	public void move(double x, double y, double z) {
		Pos next = this.pos.ofAdded(x, y, z);
		TilePos tilePos = new TilePos(next);

		if (tilePos.isValidForChunk()) {
			if (Tile.BY_ID[this.world.readTile(tilePos)].isOpaque()) {
				return;
			}
		}

		this.pos.set(next);
		this.camera.translateScene(new Vector3f((float) -x, (float) -y, (float) -z));
	}
}
